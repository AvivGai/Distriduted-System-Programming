import java.io.Serializable;

public class TitleReviews implements Serializable {
    private String title;
    private Review[] reviews;


    public TitleReviews(String title, Review[] reviews) {
        this.title = title;
        this.reviews = reviews;
    }

    public String getTitle() {
        return title;
    }

    public Review[] getReviews() {
        return reviews;
    }
}
