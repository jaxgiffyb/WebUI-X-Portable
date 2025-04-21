package com.dergoogler.mmrl.webuix.model

import androidx.annotation.DrawableRes
import com.dergoogler.mmrl.platform.Platform

data class FeaturedManager(
    val name: String,
    @DrawableRes val icon: Int,
    val platform: Platform
)