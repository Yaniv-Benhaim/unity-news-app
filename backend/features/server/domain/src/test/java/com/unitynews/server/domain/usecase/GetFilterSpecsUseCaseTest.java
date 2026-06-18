package com.unitynews.server.domain.usecase;

import static org.junit.Assert.assertEquals;

import com.unitynews.server.domain.model.Article;
import com.unitynews.server.domain.model.FilterSpec;
import com.unitynews.server.domain.model.FilterType;
import java.util.Collections;
import java.util.List;
import org.junit.Test;

public class GetFilterSpecsUseCaseTest {

    private final GetFilterSpecsUseCase useCase = new GetFilterSpecsUseCase();

    private final List<Article> articles = List.of(
            article("1", 5),
            article("2", 2),
            article("3", 5),
            article("4", 4)
    );

    @Test
    public void specsAreTitleThenRating() {
        List<FilterSpec> result = useCase.invoke(articles);

        assertEquals(
                List.of(
                        new FilterSpec("title", "Title", FilterType.Text, Collections.emptyList()),
                        new FilterSpec("rating", "Rating", FilterType.MultiSelect, List.of("2", "4", "5"))
                ),
                result
        );
    }

    @Test
    public void ratingOptionsAreDerivedFromDatasetDistinctSortedStringValues() {
        List<FilterSpec> result = useCase.invoke(articles);

        assertEquals(List.of("2", "4", "5"), result.get(1).getOptions());
    }

    @Test
    public void emptyDatasetStillReturnsTitleAndRatingSpecsWithEmptyRatingOptions() {
        List<FilterSpec> result = useCase.invoke(Collections.emptyList());

        assertEquals(
                List.of(
                        new FilterSpec("title", "Title", FilterType.Text, Collections.emptyList()),
                        new FilterSpec("rating", "Rating", FilterType.MultiSelect, Collections.emptyList())
                ),
                result
        );
    }

    private Article article(String id, int rating) {
        return new Article(
                id,
                "Article " + id,
                "Description " + id,
                "https://example.com/" + id + ".png",
                rating,
                10,
                20,
                30
        );
    }
}
