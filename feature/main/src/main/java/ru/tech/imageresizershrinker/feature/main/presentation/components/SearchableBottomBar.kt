/*
 * ImageToolbox is an image editor for android
 * Copyright (c) 2024 T8RIN (Malik Mukhametzyanov)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * You should have received a copy of the Apache License
 * along with this program.  If not, see <http://www.apache.org/licenses/LICENSE-2.0>.
 */

package ru.tech.imageresizershrinker.feature.main.presentation.components

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import ru.tech.imageresizershrinker.core.domain.APP_LINK
import ru.tech.imageresizershrinker.core.resources.BuildConfig
import ru.tech.imageresizershrinker.core.resources.R
import ru.tech.imageresizershrinker.core.resources.material.Github
import ru.tech.imageresizershrinker.core.resources.material.GooglePlay
import ru.tech.imageresizershrinker.core.ui.theme.outlineVariant
import ru.tech.imageresizershrinker.core.ui.utils.helper.ContextUtils.isInstalledFromPlayStore
import ru.tech.imageresizershrinker.core.ui.widget.buttons.EnhancedButton
import ru.tech.imageresizershrinker.core.ui.widget.buttons.EnhancedFloatingActionButton
import ru.tech.imageresizershrinker.core.ui.widget.buttons.EnhancedIconButton
import ru.tech.imageresizershrinker.core.ui.widget.modifier.drawHorizontalStroke
import ru.tech.imageresizershrinker.core.ui.widget.modifier.pulsate
import ru.tech.imageresizershrinker.core.ui.widget.text.RoundedTextField

@Composable
internal fun SearchableBottomBar(
    searching: Boolean,
    updateAvailable: Boolean,
    onTryGetUpdate: () -> Unit,
    screenSearchKeyword: String,
    onUpdateSearch: (String) -> Unit,
    onCloseSearch: () -> Unit
) {
    BottomAppBar(
        modifier = Modifier.drawHorizontalStroke(top = true),
        actions = {
            if (!searching) {
                EnhancedButton(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer.copy(
                        alpha = 0.5f
                    ),
                    borderColor = MaterialTheme.colorScheme.outlineVariant(
                        onTopOf = MaterialTheme.colorScheme.secondaryContainer
                    ),
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .pulsate(enabled = updateAvailable),
                    onClick = onTryGetUpdate
                ) {
                    Text(
                        stringResource(R.string.version) + " ${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})"
                    )
                }
            } else {
                BackHandler {
                    onUpdateSearch("")
                    onCloseSearch()
                }
                ProvideTextStyle(value = MaterialTheme.typography.bodyLarge) {
                    RoundedTextField(
                        maxLines = 1,
                        hint = { Text(stringResource(id = R.string.search_here)) },
                        modifier = Modifier
                            .padding(start = 6.dp)
                            .offset(2.dp, (-2).dp),
                        keyboardOptions = KeyboardOptions.Default.copy(
                            imeAction = ImeAction.Search
                        ),
                        value = screenSearchKeyword,
                        onValueChange = {
                            onUpdateSearch(it)
                        },
                        startIcon = {
                            EnhancedIconButton(
                                containerColor = Color.Transparent,
                                contentColor = LocalContentColor.current,
                                enableAutoShadowAndBorder = false,
                                onClick = {
                                    onUpdateSearch("")
                                    onCloseSearch()
                                },
                                modifier = Modifier.padding(start = 4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        },
                        endIcon = {
                            AnimatedVisibility(
                                visible = screenSearchKeyword.isNotEmpty(),
                                enter = fadeIn() + scaleIn(),
                                exit = fadeOut() + scaleOut()
                            ) {
                                EnhancedIconButton(
                                    containerColor = Color.Transparent,
                                    contentColor = LocalContentColor.current,
                                    enableAutoShadowAndBorder = false,
                                    onClick = {
                                        onUpdateSearch("")
                                    },
                                    modifier = Modifier.padding(end = 4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Close,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        },
                        shape = CircleShape
                    )
                }
            }
        },
        floatingActionButton = {
            val context = LocalContext.current
            if (!searching) {
                EnhancedFloatingActionButton(
                    onClick = {
                        if (context.isInstalledFromPlayStore()) {
                            try {
                                context.startActivity(
                                    Intent(
                                        Intent.ACTION_VIEW,
                                        Uri.parse("market://details?id=${context.packageName}")
                                    )
                                )
                            } catch (e: ActivityNotFoundException) {
                                context.startActivity(
                                    Intent(
                                        Intent.ACTION_VIEW,
                                        Uri.parse("https://play.google.com/store/apps/details?id=${context.packageName}")
                                    )
                                )
                            }
                        } else {
                            context.startActivity(
                                Intent(
                                    Intent.ACTION_VIEW,
                                    Uri.parse(APP_LINK)
                                )
                            )
                        }
                    },
                    modifier = Modifier
                        .requiredSize(size = 56.dp),
                    content = {
                        if (context.isInstalledFromPlayStore()) {
                            Icon(
                                imageVector = Icons.Rounded.GooglePlay,
                                contentDescription = null,
                                modifier = Modifier.offset(1.5.dp)
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Rounded.Github,
                                contentDescription = null
                            )
                        }
                    }
                )
            }
        }
    )
}