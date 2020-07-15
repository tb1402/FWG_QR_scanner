package de.fwg.qr.scanner.progress;

public class visitedStation {

    public String StationId;
    public String StationName;
    public boolean Visited;

    public visitedStation(String stationId, String stationName, boolean visited){
        StationId = stationId;
        StationName = stationName;
        Visited = visited;
    }
}
