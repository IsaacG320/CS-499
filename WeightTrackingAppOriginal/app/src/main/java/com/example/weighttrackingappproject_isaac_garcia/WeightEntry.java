package com.example.weighttrackingappproject_isaac_garcia;

public class WeightEntry
{

    private long id;
    private String date;
    private String weight;

    public WeightEntry(long id, String date, String weight)
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

    public String getWeight()
    {
        return weight;
    }
}




