package com.mixcom.chat

import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.gotrue.Auth
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.realtime.Realtime

object SupabaseClientProvider {
    val client = createSupabaseClient(
        supabaseUrl = "https://bwowvsrcehixusbkhctv.supabase.co",
        supabaseKey = "sb_publishable_ohZfbMRrAR2GwR-GtytDhA_WZ0ZknTK"
    ) {
        install(Auth)
        install(Postgrest)
        install(Realtime)
    }
}
