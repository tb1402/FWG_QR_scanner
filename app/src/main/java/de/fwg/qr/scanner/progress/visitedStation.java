package de.fwg.qr.scanner.progress;

import java.util.Date;

public class visitedStation {

    public String StationId;
    public String StationName;
    public boolean Visited;
    public Date LastVisited;

    public visitedStation(String stationId, String stationName, boolean visited){
        StationId = stationId;
        StationName = stationName;
        Visited = visited;
        if (!Visited) LastVisited = null;
    }

    public visitedStation(String stationId, String stationName, boolean visited, Date lastVisited){
        this(stationId, stationName, visited);
        LastVisited = lastVisited;
    }

}
