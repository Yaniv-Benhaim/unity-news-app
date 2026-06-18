package com.unitynews.server.domain.usecase;

import static org.junit.Assert.assertEquals;

import com.unitynews.server.domain.model.Article;
import com.unitynews.server.domain.model.FilterCriteria;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import org.junit.Test;

public class FilterArticlesUseCaseTest {

    private final FilterArticlesUseCase useCase = new FilterArticlesUseCase();

    private final List<Article> articles = List.of(
            article("1", "Unity launches new editor", 5),
            article("2", "Android app architecture", 4),
            article("3", "UNITY game services update", 3),
            article("4", "Backend server guide", 5)
    );

    @Test
    public void emptyCriteriaReturnsAllArticles() {
        List<Article> result = useCase.invoke(articles, new FilterCriteria());

        assertEquals(articles, result);
    }

    @Test
    public void titleFilterIsCaseInsensitiveContains() {
        List<Article> result = useCase.invoke(articles, new FilterCriteria("unity", Collections.emptySet()));

        assertEquals(List.of(articles.get(0), articles.get(2)), result);
    }

    @Test
    public void blankTitleReturnsAllArticles() {
        List<Article> result = useCase.invoke(articles, new FilterCriteria("   ", Collections.emptySet()));

        assertEquals(articles, result);
    }

    @Test
    public void ratingFilterSupportsMultipleValues() {
        List<Article> result = useCase.invoke(articles, new FilterCriteria(null, Set.of(3, 5)));

        assertEquals(List.of(articles.get(0), articles.get(2), articles.get(3)), result);
    }

    @Test
    public void titleAndRatingFiltersAreAppliedTogether() {
        List<Article> result = useCase.invoke(articles, new FilterCriteria("unity", Set.of(3)));

        assertEquals(List.of(articles.get(2)), result);
    }

    @Test
    public void noMatchesReturnsEmptyList() {
        List<Article> result = useCase.invoke(articles, new FilterCriteria("unity", Set.of(1)));

        assertEquals(Collections.emptyList(), result);
    }

    private Article article(String id, String title, int rating) {
        return new Article(
                id,
                title,
                "Description " + id,
                "https://example.com/" + id + ".png",
                rating,
                10,
                20,
                30
        );
    }
}
