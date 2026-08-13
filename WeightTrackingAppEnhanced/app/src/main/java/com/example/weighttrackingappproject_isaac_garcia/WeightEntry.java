package com.example.weighttrackingappproject_isaac_garcia;

public class WeightEntry
{
    private final long id;
    private final String date;
    private final double weight;

    public WeightEntry(
            long id,
            String date,
            double weight)
    {
        this.id = id;
        this.date = date;
        this.weight = weight;
    }

    public long getId()
    {
        return id;
    }

    public String getDate()
    {
        return date;
    }

    public double getWeightValue()
    {
        return weight;
    }
}