package isel.leic.pc.lec_05_02.completable_futures.service

import isel.leic.pc.lec_05_02.completable_futures.api.Album
import isel.leic.pc.lec_05_02.completable_futures.api.MusicApi
import java.util.concurrent.CompletableFuture

class MusicService(val api: MusicApi) {

    fun getArtistGenre(artistName : String) : CompletableFuture<String> {
        return api.getArtist(artistName)
            .thenApply {
                it.genre
            }
    }

    fun getArtistAlbums(artistName: String) :
            CompletableFuture<List<Album>> {
        return api.getArtist(artistName)
            .thenCompose {
                api.getArtistAlbums(it.id)
            }
    }

    fun getArtistListenersNumber(artistName: String)
            : CompletableFuture<Int> {
       TODO()

    }

    fun areArtistsSameGenre(artistName1: String, artistName2: String)
            : CompletableFuture<Boolean> {
        return api.getArtist(artistName1)
            .thenCompose { a1 ->
                api.getArtist(artistName2)
                .thenApply { a2 ->
                    a1.genre == a2.genre
                }
            }
    }

    fun areArtistsSameGenre2(artistName1: String, artistName2: String)
            : CompletableFuture<Boolean> {
        return api.getArtist(artistName1)
            .thenCombine(api.getArtist(artistName2)) {
                    a1, a2 ->  a1.genre == a2.genre
            }
    }

}