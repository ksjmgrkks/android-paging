package com.example.android.codelabs.paging.data

import androidx.paging.PagingSource
import androidx.paging.PagingState
import kotlinx.coroutines.delay
import java.lang.Integer.max
import java.time.LocalDateTime

private val firstArticleCreatedTime = LocalDateTime.now()

class ArticlePagingSource : PagingSource<Int, Article>() {
    companion object {
        private const val STARTING_KEY = 0
        private const val LOAD_DELAY_MILLIS = 3_000L
    }
    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Article> {
        // 첫 번째 로드인 경우 STARTING_KEY로 페이징 시작
        val start = params.key ?: STARTING_KEY
        // params.loadSize 만큼의 항목을 불러옵니다.
        val range = start.until(start + params.loadSize)

        if (start != STARTING_KEY) delay(LOAD_DELAY_MILLIS)
        return LoadResult.Page(
            data = range.map { number ->
                Article(
                    // 연속적으로 증가하는 기사id 숫자 생성
                    id = number,
                    title = "Article $number",
                    description = "This describes article $number",
                    created = firstArticleCreatedTime.minusDays(number.toLong())
                )
            },

            // STARTING KEY 뒤에 항목을 불러오지 않도록 합니다.
            prevKey = when (start) {
                STARTING_KEY -> null
                else -> ensureValidKey(key = range.first - params.loadSize)
            },
            nextKey = range.last + 1
        )
    }

    // refresh key는 invalidation 후 다음 PagingSource의 초기 로드에 사용됩니다.
    override fun getRefreshKey(state: PagingState<Int, Article>): Int? {
        // In our case we grab the item closest to the anchor position
        // 우리의 경우 anchor position(데이터를 성공적으로 가져온 마지막 index)에
        // 가장 가까운 아이템을 찾습니다. state.closestItemToPosition(anchorPosition)
        // then return its id - (state.config.pageSize / 2) as a buffer
        // 그런 다음 해당 ID를 반환합니다. - (state.config.pageSize / 2)를 버퍼로 반환합니다.
        val anchorPosition = state.anchorPosition ?: return null
        val article = state.closestItemToPosition(anchorPosition) ?: return null
        return ensureValidKey(key = article.id - (state.config.pageSize / 2))
    }


    /**
     * 페이징 키가 [STARTING_KEY] 이상인지 확인합니다.
     */
    private fun ensureValidKey(key: Int) = max(STARTING_KEY, key)


}