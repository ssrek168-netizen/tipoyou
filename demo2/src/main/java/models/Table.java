package models;

public class Table {
    private int number;
    private boolean occupied;
    private int seats;

    public Table(int number, int seats) {
        this.number = number;
        this.seats = seats;
        this.occupied = false;
    }

    public int getNumber() { return number; }
    public boolean isOccupied() { return occupied; }
    public void setOccupied(boolean occupied) { this.occupied = occupied; }
    public int getSeats() { return seats; }
}