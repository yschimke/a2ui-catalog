package ee.schimke.a2uicatalog.harness

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.painter.BrushPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

/**
 * The media renderers a sticker sheet can stand behind.
 *
 * Each one is a function of its arguments and nothing else: no decoder, no network, no clock. The
 * URL still matters — it picks the artwork — so two stickers naming different URLs draw different
 * pictures, and one naming the same URL draws the same one on every run.
 */
object StickerMedia {

  private const val INTRINSIC = 1000f

  /** Three-stop gradients, picked by URL. A painter from constants cannot decode differently. */
  private val palettes =
    listOf(
      listOf(Color(0xFF3C8CDE), Color(0xFFED73A8), Color(0xFFE763F9)),
      listOf(Color(0xFF1B6D4F), Color(0xFF8BD3A5), Color(0xFFF4E3A1)),
      listOf(Color(0xFF6B3FA0), Color(0xFFB08EE6), Color(0xFFF2C6DE)),
      listOf(Color(0xFF8A4B08), Color(0xFFF2A65A), Color(0xFFFCE1B8)),
    )

  private fun paletteFor(url: String) = palettes[Math.floorMod(url.hashCode(), palettes.size)]

  /**
   * Artwork standing in for the photograph at [url], with a large intrinsic size so a `fit` scales
   * it the way a loaded bitmap would be scaled.
   */
  fun artwork(url: String): BrushPainter {
    val (a, b, c) = paletteFor(url)
    return BrushPainter(
      Brush.linearGradient(
        0.0f to a,
        0.5f to b,
        1.0f to c,
        start = Offset.Zero,
        end = Offset(INTRINSIC, INTRINSIC),
      )
    )
  }

  @Composable
  fun Image(
    url: String,
    contentDescription: String?,
    contentScale: ContentScale,
    modifier: Modifier,
  ) {
    Image(
      painter = artwork(url),
      contentDescription = contentDescription,
      contentScale = contentScale,
      modifier = modifier,
    )
  }

  /** A 16:9 poster frame with a play affordance: what a video looks like before it plays. */
  @Composable
  fun Video(url: String, modifier: Modifier) {
    Box(
      modifier =
        modifier.fillMaxWidth().aspectRatio(16f / 9f).clip(RoundedCornerShape(12.dp)).semantics {
          contentDescription = "Video"
        },
      contentAlignment = Alignment.Center,
    ) {
      Image(url, null, ContentScale.Crop, Modifier.matchParentSize())
      Box(
        modifier =
          Modifier.size(56.dp).clip(CircleShape).background(Color.Black.copy(alpha = 0.55f)),
        contentAlignment = Alignment.Center,
      ) {
        PlayGlyph(Color.White)
      }
    }
  }

  /** A Material audio row: a play button, the track's description and a progress bar at rest. */
  @Composable
  fun AudioPlayer(url: String, contentDescription: String?, modifier: Modifier) {
    Surface(
      modifier = modifier.fillMaxWidth(),
      shape = RoundedCornerShape(16.dp),
      color = MaterialTheme.colorScheme.surfaceContainerHigh,
    ) {
      Row(
        modifier = Modifier.padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
      ) {
        FilledIconButton(onClick = {}) { PlayGlyph(MaterialTheme.colorScheme.onPrimary) }
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
          Text(
            text = contentDescription ?: url.substringAfterLast('/'),
            style = MaterialTheme.typography.titleSmall,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
          )
          LinearProgressIndicator(progress = { 0.35f }, modifier = Modifier.fillMaxWidth())
        }
      }
    }
  }

  @Composable
  private fun PlayGlyph(color: Color) {
    Canvas(Modifier.size(20.dp)) {
      val path =
        Path().apply {
          moveTo(size.width * 0.2f, 0f)
          lineTo(size.width, size.height / 2f)
          lineTo(size.width * 0.2f, size.height)
          close()
        }
      drawPath(path, color)
    }
  }
}
