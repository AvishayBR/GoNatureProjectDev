package Entities;

import java.io.Serializable;
import java.util.*;

public class ParkBank implements Serializable, Iterable<Park> {
    private ArrayList<Park> parks;

    private static final Map<String, String> ParkMap;

    static {
        Map<String, String> aMap = new HashMap<>();
        aMap.put("Green Oasis Park", "1");
        aMap.put("Golden Gardens", "2");
        aMap.put("Mountain View Park", "3");
        aMap.put("Sunset Park", "4");
        // Additional entries can be added here
        ParkMap = Collections.unmodifiableMap(aMap);
    }

    public static String getParkNameByID(String value) {
        for (Map.Entry<String, String> entry : ParkMap.entrySet()) {
            if (entry.getValue().equals(value)) {
                return entry.getKey();
            }
        }
        // If the value is not found, return null or throw an exception as needed
        return null;
    }

    public static Map<String, String> getUnmodifiableMap() {
        return ParkMap;
    }

    public ParkBank() {
        parks = new ArrayList<>();
    }

    public ParkBank(Park park) {
        parks = new ArrayList<>();
        parks.add(park);
    }

    public Boolean insertPark(Park park) {
        return parks.add(park);
    }


    public void deletePark(Park park) {
        if (parks.contains(park))
            parks.remove(park);
    }

    public Boolean isExists(Park park) {
        return parks.contains(park);
    }

    public ParkBank GetParksByDepartmentID(Integer departmentID) {
        ParkBank parkBank = new ParkBank();
        for (Park park : parks) {
            if (park.getDepartment() == departmentID)
                parkBank.insertPark(park);
        }
        return parkBank;
    }

    @Override
    public Iterator<Park> iterator() {
        return new ParkBank.ParkIterator();
    }

    private class ParkIterator implements Iterator<Park> {
        private int currentIndex = 0;

        @Override
        public boolean hasNext() {
            return currentIndex < parks.size();
        }

        @Override
        public Park next() {
            if (!hasNext()) {
                throw new NoSuchElementException("No more elements in the ParkBank");
            }
            return parks.get(currentIndex++);
        }

    }
}
