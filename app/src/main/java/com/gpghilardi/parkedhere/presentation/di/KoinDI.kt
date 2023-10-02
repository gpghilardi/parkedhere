package com.gpghilardi.parkedhere.presentation.di

import com.gpghilardi.parkedhere.presentation.repository.LocationStorage
import com.gpghilardi.parkedhere.presentation.viewmodel.LocationViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    single { LocationStorage(get()) }

    viewModel { LocationViewModel(get(), get()) }
}