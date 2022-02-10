package com.example.password_manager.unittest

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.password_manager.R
import com.google.common.truth.Truth.assertThat
import org.junit.Test

class ResourceComparerTest{
    private val resourceComparer = ResourceComparer()

    @Test
    fun stringResourceSameAsGivenString_returnTrue(){
        val context = ApplicationProvider.getApplicationContext<Context>()
        val result = resourceComparer.isEqual(context, R.string.app_name, "password manager")

        assertThat(result).isTrue()
    }

    @Test
    fun stringResourceDifferentAsGivenString_returnFalse(){
        val context = ApplicationProvider.getApplicationContext<Context>()
        val result = resourceComparer.isEqual(context, R.string.app_name, "hello world")

        assertThat(result).isFalse()
    }
}