package com.parkedhere.app.presentation.di

import com.parkedhere.app.presentation.repository.LocationStorage
import com.parkedhere.app.presentation.viewmodel.LocationViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    single { LocationStorage(get()) }

    viewModel { LocationViewModel(get(), get()) }
}