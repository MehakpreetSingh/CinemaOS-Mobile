package com.nuvio.app.core.network

import okhttp3.Dns
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.OkHttpClient
import okhttp3.dnsoverhttps.DnsOverHttps
import java.net.InetAddress
import java.net.UnknownHostException
import java.util.concurrent.TimeUnit

/**
 * Shared DNS-over-HTTPS resolver. Falls back to the system resolver only when DoH fails, so ISP level
 * DNS blocking (for example of TMDB) is bypassed: Cloudflare first, then Google, then Dns.SYSTEM.
 */
object DohDns : Dns {
    private val bootstrapClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(4, TimeUnit.SECONDS)
            .readTimeout(4, TimeUnit.SECONDS)
            .callTimeout(6, TimeUnit.SECONDS)
            .build()
    }

    private val cloudflare: DnsOverHttps by lazy {
        DnsOverHttps.Builder()
            .client(bootstrapClient)
            .url("https://cloudflare-dns.com/dns-query".toHttpUrl())
            .bootstrapDnsHosts(
                InetAddress.getByName("1.1.1.1"),
                InetAddress.getByName("1.0.0.1"),
                InetAddress.getByName("2606:4700:4700::1111"),
            )
            .build()
    }

    private val google: DnsOverHttps by lazy {
        DnsOverHttps.Builder()
            .client(bootstrapClient)
            .url("https://dns.google/dns-query".toHttpUrl())
            .bootstrapDnsHosts(
                InetAddress.getByName("8.8.8.8"),
                InetAddress.getByName("8.8.4.4"),
            )
            .build()
    }

    override fun lookup(hostname: String): List<InetAddress> {
        // Literal IPs and the DoH hosts themselves never need DoH.
        if (hostname == "cloudflare-dns.com" || hostname == "dns.google") {
            return Dns.SYSTEM.lookup(hostname)
        }
        try {
            val result = cloudflare.lookup(hostname)
            if (result.isNotEmpty()) return result
        } catch (_: Exception) {
        }
        try {
            val result = google.lookup(hostname)
            if (result.isNotEmpty()) return result
        } catch (_: Exception) {
        }
        try {
            return Dns.SYSTEM.lookup(hostname)
        } catch (e: UnknownHostException) {
            throw e
        }
    }
}
