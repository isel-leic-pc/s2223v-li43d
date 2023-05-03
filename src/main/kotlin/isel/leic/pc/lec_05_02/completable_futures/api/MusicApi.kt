package isel.leic.pc.lec_05_02.completable_futures.api

import java.util.concurrent.CompletableFuture

interface MusicApi {
    fun getArtist(name: String) : CompletableFuture<Artist>

    fun getArtistDetail(artistId: Int) : CompletableFuture<ArtistDetail>

    fun getArtistAlbums(artistId : Int) : CompletableFuture<List<Album>>

}