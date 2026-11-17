package com.nuvio.app.features.player

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.addPathNodes
import androidx.compose.ui.unit.dp

/**
 * The CinemaOS web player's solid "noir" icon set, drawn on the same 24px grid with the same
 * path data so the mobile controls match the website shape for shape.
 */
internal object CinemaosPlayerIcons {

    private fun icon(name: String, block: ImageVector.Builder.() -> Unit): ImageVector =
        ImageVector.Builder(
            name = name,
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f,
        ).apply(block).build()

    private fun ImageVector.Builder.fill(d: String, evenOdd: Boolean = false, alpha: Float = 1f) {
        addPath(
            pathData = addPathNodes(d),
            pathFillType = if (evenOdd) PathFillType.EvenOdd else PathFillType.NonZero,
            fill = SolidColor(Color.Black),
            fillAlpha = alpha,
        )
    }

    private fun ImageVector.Builder.stroke(d: String, width: Float) {
        addPath(
            pathData = addPathNodes(d),
            stroke = SolidColor(Color.Black),
            strokeLineWidth = width,
            strokeLineCap = StrokeCap.Round,
            strokeLineJoin = StrokeJoin.Round,
        )
    }

    private fun rect(x: Float, y: Float, w: Float, h: Float, r: Float): String =
        "M${x + r} ${y}H${x + w - r}A$r $r 0 0 1 ${x + w} ${y + r}V${y + h - r}" +
            "A$r $r 0 0 1 ${x + w - r} ${y + h}H${x + r}A$r $r 0 0 1 $x ${y + h - r}" +
            "V${y + r}A$r $r 0 0 1 ${x + r} ${y}Z"

    private fun circle(cx: Float, cy: Float, r: Float): String =
        "M${cx - r} ${cy}a$r $r 0 1 0 ${r * 2} 0a$r $r 0 1 0 ${-r * 2} 0Z"

    val Play: ImageVector by lazy {
        icon("CinemaosPlay") {
            fill("M5.2 4.9v14.2c0 1.5 1.6 2.4 2.9 1.6l11.2-7.1c1.2-.7 1.2-2.5 0-3.2L8.1 3.3C6.8 2.5 5.2 3.4 5.2 4.9z")
        }
    }

    val Pause: ImageVector by lazy {
        icon("CinemaosPause") {
            fill(rect(5f, 3.5f, 5f, 17f, 2f))
            fill(rect(14f, 3.5f, 5f, 17f, 2f))
        }
    }

    /** Arc and arrowhead of the "seek 10" glyph; the "10" is drawn as text on top. */
    val SeekBack: ImageVector by lazy {
        icon("CinemaosSeekBack") {
            stroke("M11.6 3.8A8.2 8.2 0 1 1 3.8 12", 2.2f)
            fill("M7.6 3.8c0-.4.2-.7.5-.9l3.7-2.4c.7-.4 1.5.1 1.5.9v4.8c0 .8-.8 1.3-1.5.9L8.1 4.7c-.3-.2-.5-.5-.5-.9z")
        }
    }

    val Back: ImageVector by lazy {
        icon("CinemaosBack") { stroke("M15 4.6 7.6 12l7.4 7.4", 2.8f) }
    }

    val Subtitles: ImageVector by lazy {
        icon("CinemaosSubtitles") {
            fill(
                "M5 4h14a3.5 3.5 0 0 1 3.5 3.5v9A3.5 3.5 0 0 1 19 20H5a3.5 3.5 0 0 1-3.5-3.5v-9A3.5 3.5 0 0 1 5 4z" +
                    "m1.9 7.6a1 1 0 0 0 0 2h3a1 1 0 0 0 0-2h-3z" +
                    "m6.2 0a1 1 0 0 0 0 2h4a1 1 0 0 0 0-2h-4z" +
                    "m-6.2 3.5a1 1 0 0 0 0 2h6.2a1 1 0 0 0 0-2H6.9z" +
                    "m9.4 0a1 1 0 0 0 0 2h.8a1 1 0 0 0 0-2h-.8z",
                evenOdd = true,
            )
        }
    }

    val Audio: ImageVector by lazy {
        icon("CinemaosAudio") {
            fill(rect(2.6f, 9.5f, 2.8f, 5f, 1.4f))
            fill(rect(7.1f, 6f, 2.8f, 12f, 1.4f))
            fill(rect(11.6f, 2.8f, 2.8f, 18.4f, 1.4f))
            fill(rect(16.1f, 7f, 2.8f, 10f, 1.4f))
            fill(rect(20.6f, 10.2f, 1.8f, 3.6f, 0.9f))
        }
    }

    val Settings: ImageVector by lazy {
        icon("CinemaosSettings") {
            fill(
                "M10.4 1.8h3.2c.7 0 1.3.5 1.4 1.2l.3 1.9c.6.3 1.2.6 1.7 1l1.8-.7c.7-.3 1.4 0 1.8.6l1.6 2.8c.4.6.2 1.4-.3 1.8l-1.5 1.2a7.6 7.6 0 0 1 0 2l1.5 1.2c.5.4.7 1.2.3 1.8l-1.6 2.8c-.4.6-1.1.9-1.8.6l-1.8-.7c-.5.4-1.1.7-1.7 1l-.3 1.9c-.1.7-.7 1.2-1.4 1.2h-3.2c-.7 0-1.3-.5-1.4-1.2l-.3-1.9c-.6-.3-1.2-.6-1.7-1l-1.8.7c-.7.3-1.4 0-1.8-.6l-1.6-2.8c-.4-.6-.2-1.4.3-1.8l1.5-1.2a7.6 7.6 0 0 1 0-2L2.7 9.6c-.5-.4-.7-1.2-.3-1.8L4 5c.4-.6 1.1-.9 1.8-.6l1.8.7c.5-.4 1.1-.7 1.7-1l.3-1.9c.1-.7.7-1.2 1.4-1.2z" +
                    "M12 8.4a3.6 3.6 0 1 0 0 7.2 3.6 3.6 0 0 0 0-7.2z",
                evenOdd = true,
            )
        }
    }

    val Sources: ImageVector by lazy {
        icon("CinemaosSources") {
            fill("M11 2.6c.6-.3 1.4-.3 2 0l8 4.1c1 .5 1 2 0 2.5l-8 4.1c-.6.3-1.4.3-2 0l-8-4.1c-1-.5-1-2 0-2.5l8-4.1z")
            stroke("M3.2 12.6l8.8 4.5 8.8-4.5", 2.2f)
            stroke("M3.2 16.6l8.8 4.5 8.8-4.5", 2.2f)
        }
    }

    /** The "HD" badge, used for stream info. */
    val Stats: ImageVector by lazy {
        icon("CinemaosStats") {
            fill(
                rect(1.5f, 4.5f, 21f, 15f, 4f) +
                    "M6.4 8.8c.6 0 1 .4 1 1v1.4h2.2V9.8c0-.6.4-1 1-1s1 .4 1 1v4.4c0 .6-.4 1-1 1s-1-.4-1-1v-1.1H7.4v1.1c0 .6-.4 1-1 1s-1-.4-1-1V9.8c0-.6.4-1 1-1z" +
                    "m7.1 0h2c1.9 0 3.2 1.3 3.2 3.2s-1.3 3.2-3.2 3.2h-2c-.6 0-1-.4-1-1V9.8c0-.6.4-1 1-1z" +
                    "m1 1.9v2.6h1c.8 0 1.3-.5 1.3-1.3s-.5-1.3-1.3-1.3h-1z",
                evenOdd = true,
            )
        }
    }

    val Unlock: ImageVector by lazy {
        icon("CinemaosUnlock") {
            stroke("M7.6 10.5V8a4.4 4.4 0 0 1 8.5-1.6", 2.4f)
            fill(
                "M6.6 10h10.8a2.6 2.6 0 0 1 2.6 2.6v6.3a2.6 2.6 0 0 1-2.6 2.6H6.6A2.6 2.6 0 0 1 4 18.9v-6.3A2.6 2.6 0 0 1 6.6 10z" +
                    "m5.4 3.4a1.3 1.3 0 0 0-.9 2.2v1.6a.9.9 0 0 0 1.8 0v-1.6a1.3 1.3 0 0 0-.9-2.2z",
                evenOdd = true,
            )
        }
    }

    val Lock: ImageVector by lazy {
        icon("CinemaosLock") {
            stroke("M7.6 10.5V8a4.4 4.4 0 0 1 8.8 0v2.5", 2.4f)
            fill(
                "M6.6 10h10.8a2.6 2.6 0 0 1 2.6 2.6v6.3a2.6 2.6 0 0 1-2.6 2.6H6.6A2.6 2.6 0 0 1 4 18.9v-6.3A2.6 2.6 0 0 1 6.6 10z" +
                    "m5.4 3.4a1.3 1.3 0 0 0-.9 2.2v1.6a.9.9 0 0 0 1.8 0v-1.6a1.3 1.3 0 0 0-.9-2.2z",
                evenOdd = true,
            )
        }
    }

    val Episodes: ImageVector by lazy {
        icon("CinemaosEpisodes") {
            fill(rect(5.5f, 2.6f, 13f, 2.4f, 1.2f), alpha = 0.6f)
            fill(
                "M5 6.6h14a3.4 3.4 0 0 1 3.4 3.4v7.6a3.4 3.4 0 0 1-3.4 3.4H5a3.4 3.4 0 0 1-3.4-3.4V10A3.4 3.4 0 0 1 5 6.6z" +
                    "m5.4 4.15v6.1c0 .75.82 1.2 1.45.8l4.75-3.05c.58-.37.58-1.23 0-1.6l-4.75-3.05c-.63-.4-1.45.05-1.45.8z",
                evenOdd = true,
            )
        }
    }

    val Next: ImageVector by lazy {
        icon("CinemaosNext") {
            fill("M3.5 5.3v13.4c0 1.3 1.4 2 2.5 1.3l9.7-6.7c.9-.6.9-2 0-2.6L6 4c-1.1-.7-2.5 0-2.5 1.3z")
            fill(rect(17f, 3.6f, 3.6f, 16.8f, 1.8f))
        }
    }

    val Cast: ImageVector by lazy {
        icon("CinemaosCast") {
            fill(
                "M5 3.5h14A3.5 3.5 0 0 1 22.5 7v10a3.5 3.5 0 0 1-3.5 3.5h-4.6a1.1 1.1 0 0 1 0-2.2H19c.7 0 1.3-.6 1.3-1.3V7c0-.7-.6-1.3-1.3-1.3H5c-.7 0-1.3.6-1.3 1.3v1.4a1.1 1.1 0 0 1-2.2 0V7A3.5 3.5 0 0 1 5 3.5z",
                evenOdd = true,
            )
            stroke("M2.6 12.2a8 8 0 0 1 8.2 8.2", 2.2f)
            stroke("M2.6 16a4.4 4.4 0 0 1 4.4 4.4", 2.2f)
            fill(circle(3.1f, 20f, 1.5f))
        }
    }

    val Party: ImageVector by lazy {
        icon("CinemaosParty") {
            fill(circle(9f, 7.6f, 3.9f))
            fill("M1.8 19.6c0-3.6 3.2-6.4 7.2-6.4s7.2 2.8 7.2 6.4c0 .8-.6 1.4-1.4 1.4H3.2c-.8 0-1.4-.6-1.4-1.4z")
            fill(circle(17.4f, 8.6f, 2.9f), alpha = 0.6f)
            fill("M17.6 13.4c2.7.2 4.6 2.3 4.6 4.8 0 .6-.5 1.1-1.1 1.1h-3.4c0-2.3-.9-4.4-2.4-5.6.7-.2 1.5-.3 2.3-.3z", alpha = 0.6f)
        }
    }

    val Monitor: ImageVector by lazy {
        icon("CinemaosMonitor") {
            fill(
                "M5 3h14a3.5 3.5 0 0 1 3.5 3.5v7.8a3.5 3.5 0 0 1-3.5 3.5H5a3.5 3.5 0 0 1-3.5-3.5V6.5A3.5 3.5 0 0 1 5 3z" +
                    "m.2 2.2c-.8 0-1.5.7-1.5 1.5v7.4c0 .8.7 1.5 1.5 1.5h13.6c.8 0 1.5-.7 1.5-1.5V6.7c0-.8-.7-1.5-1.5-1.5H5.2z",
                evenOdd = true,
            )
            fill(rect(7f, 19.4f, 10f, 2.2f, 1.1f))
        }
    }

    val Check: ImageVector by lazy {
        icon("CinemaosCheck") { stroke("M5 12.5l4.5 4.5L19 7.5", 2.6f) }
    }

    val ChevronRight: ImageVector by lazy {
        icon("CinemaosChevronRight") { stroke("M9 5l7 7-7 7", 2.2f) }
    }

    val Close: ImageVector by lazy {
        icon("CinemaosClose") { stroke("M6 6l12 12M18 6 6 18", 2.4f) }
    }
}
