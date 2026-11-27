package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.Image
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.example.R
import com.example.data.model.PosSeeds
import com.example.data.model.StaffUser
import com.example.ui.components.NonImeNumericKeypad
import com.example.ui.components.applyKeypadInput
import com.example.ui.theme.Amber300
import com.example.ui.theme.Amber500
import com.example.ui.theme.Amber950
import com.example.ui.theme.BannerCyan
import com.example.ui.theme.BannerGold
import com.example.ui.theme.Emerald500
import com.example.ui.theme.Indigo950
import com.example.ui.theme.Navy800
import com.example.ui.theme.Navy950
import com.example.ui.theme.OnDark
import com.example.ui.theme.OnGold
import com.example.ui.theme.Rose400
import com.example.ui.theme.Rose500
import com.example.ui.theme.Rose950
import com.example.ui.theme.Slate100
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate850
import com.example.ui.theme.Slate900
import com.example.ui.theme.Slate950

@Composable
fun StaffLockScreen(
    users: List<StaffUser>,
    initialSelectedUser: StaffUser?,
    pinError: String?,
    onClearError: () -> Unit,
    onUnlock: (StaffUser, String) -> Unit
) {
    val activeUsers = remember(users) {
        users.filter { it.active }.ifEmpty { PosSeeds.DEFAULT_USERS }
    }
    val sellerUser = remember(activeUsers) {
        activeUsers.find { it.role != "admin" } ?: PosSeeds.DEFAULT_USERS.first { it.role != "admin" }
    }
    val adminUser = remember(activeUsers) {
        activeUsers.find { it.role == "admin" } ?: PosSeeds.DEFAULT_USERS.first { it.role == "admin" }
    }

    var pinBuffer by remember { mutableStateOf("") }
    var showPinDigits by remember { mutableStateOf(false) }

    LaunchedEffect(pinError) {
        if (pinError != null) {
            pinBuffer = ""
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Slate950
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(Navy950, Slate950, Slate900)
                    )
                )
                .safeDrawingPadding()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            contentAlignment = Alignment.Center
        ) {
            val isCompactHeight = maxHeight < 680.dp
            val isNarrow = maxWidth < 360.dp

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 480.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(if (isCompactHeight) 10.dp else 14.dp)
            ) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    color = Navy950,
                    border = BorderStroke(1.5.dp, Amber500)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Brush.horizontalGradient(listOf(Navy950, Navy800)))
                            .padding(horizontal = 14.dp, vertical = if (isCompactHeight) 10.dp else 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = Color.White,
                            border = BorderStroke(1.5.dp, BannerCyan),
                            modifier = Modifier.size(if (isCompactHeight) 48.dp else 56.dp)
                        ) {
                            Image(
                                painter = painterResource(R.drawable.img_casaceram_minimal_logo),
                                contentDescription = "Logo CASACERAM",
                                contentScale = ContentScale.Fit,
                                modifier = Modifier.fillMaxSize().padding(4.dp)
                            )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "CASACERAM POS",
                                style = if (isNarrow) MaterialTheme.typography.titleLarge else MaterialTheme.typography.headlineSmall,
                                color = BannerGold,
                                fontWeight = FontWeight.Black,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "Vendeur sans PIN • Administrateur par PIN",
                                style = MaterialTheme.typography.bodySmall,
                                color = BannerCyan,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }

                // Direct 1-tap Seller Entry (No PIN)
                Button(
                    onClick = { onUnlock(sellerUser, "") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(if (isCompactHeight) 50.dp else 56.dp)
                        .testTag("lock_user_${sellerUser.id}"),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Emerald500, contentColor = OnDark),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
                ) {
                    Icon(Icons.Default.Storefront, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Accès Vendeur (Sans PIN)",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Black,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Warm Pearl-Porcelain Admin PIN entry card with Sapphire Header
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    color = Slate900,
                    shadowElevation = 6.dp,
                    border = BorderStroke(
                        width = 1.4.dp,
                        color = if (pinError != null) Rose500 else Slate800
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(
                            horizontal = 14.dp,
                            vertical = if (isCompactHeight) 11.dp else 14.dp
                        ),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(if (isCompactHeight) 8.dp else 12.dp)
                    ) {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            color = Navy950
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 9.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Accès ${adminUser.name}",
                                        style = MaterialTheme.typography.titleMedium,
                                        color = OnDark,
                                        fontWeight = FontWeight.Black,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = "Saisissez le code PIN à 4 chiffres",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = BannerGold,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                                IconButton(
                                    onClick = { showPinDigits = !showPinDigits },
                                    modifier = Modifier
                                        .minimumInteractiveComponentSize()
                                        .testTag("lock_pin_visibility_toggle")
                                ) {
                                    Icon(
                                        imageVector = if (showPinDigits) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                        contentDescription = if (showPinDigits) "Masquer le code" else "Afficher le code",
                                        tint = BannerGold
                                    )
                                }
                            }
                        }

                        // 4 Crisp PIN Digit Boxes
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(if (isNarrow) 10.dp else 14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val boxSize = if (isCompactHeight || isNarrow) 46.dp else 52.dp
                            repeat(4) { idx ->
                                val digit = pinBuffer.getOrNull(idx)
                                val filled = digit != null
                                val isCurrent = idx == pinBuffer.length && pinError == null
                                Surface(
                                    modifier = Modifier.size(boxSize),
                                    shape = RoundedCornerShape(14.dp),
                                    color = if (filled) Amber950 else Slate850,
                                    border = BorderStroke(
                                        width = if (filled || isCurrent) 2.dp else 1.2.dp,
                                        color = when {
                                            pinError != null -> Rose500
                                            filled -> Amber500
                                            isCurrent -> BannerGold
                                            else -> Slate700
                                        }
                                    )
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        if (filled) {
                                            if (showPinDigits) {
                                                Text(
                                                    text = digit.toString(),
                                                    style = MaterialTheme.typography.titleLarge.copy(fontSize = 20.sp),
                                                    color = Amber300,
                                                    fontWeight = FontWeight.Black
                                                )
                                            } else {
                                                Box(
                                                    modifier = Modifier
                                                        .size(12.dp)
                                                        .clip(CircleShape)
                                                        .background(BannerGold)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        if (pinError != null) {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = Rose950,
                                border = BorderStroke(1.dp, Rose500.copy(alpha = 0.65f))
                            ) {
                                Text(
                                    text = pinError,
                                    style = MaterialTheme.typography.labelLarge,
                                    color = Rose400,
                                    fontWeight = FontWeight.Black,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }
                }

                NonImeNumericKeypad(
                    onKeyPress = { key ->
                        if (pinError != null) onClearError()
                        val (next, _) = applyKeypadInput(
                            current = pinBuffer,
                            key = key,
                            allowDecimal = false,
                            isPristine = false
                        )
                        val trimmed = next.take(4)
                        pinBuffer = trimmed
                        if (trimmed.length == 4) {
                            onUnlock(adminUser, trimmed)
                        }
                    },
                    allowDecimal = false,
                    onValidate = {
                        onClearError()
                        onUnlock(adminUser, pinBuffer)
                    },
                    validateLabel = "Déverrouiller Admin",
                    accentColor = Amber500
                )
            }
        }
    }
}
