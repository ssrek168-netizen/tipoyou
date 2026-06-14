package models;

public class Review {
    private String customerName;
    private int rating;
    private String comment;

    public Review(String customerName, int rating, String comment) {
        this.customerName = customerName;
        this.rating = rating;
        this.comment = comment;
    }

    public String getCustomerName() { return customerName; }
    public int getRating() { return rating; }
    public String getComment() { return comment; }
}