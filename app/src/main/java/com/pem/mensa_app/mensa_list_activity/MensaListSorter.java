package com.pem.mensa_app.mensa_list_activity;

import com.pem.mensa_app.models.mensa.Mensa;

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;

class MensaListSorter {

    private static final Comparator<Mensa> distanceComparator = new Comparator<Mensa>() {
        @Override
        public int compare(Mensa o1, Mensa o2) {
            int visibilityResult = Integer.compare(o1.getVisibility().ordinal(), o2.getVisibility().ordinal());
            if (visibilityResult == 0) {
                int distanceResult = Double.compare(o1.getDistance(), o2.getDistance());
                if (distanceResult == 0) {
                    return o1.getName().compareTo(o2.getName());
                } else return distanceResult;
            } else return visibilityResult;
        }
    };

    private static final Comparator<Mensa> occupancyComparator = new Comparator<Mensa>() {
        @Override
        public int compare(Mensa o1, Mensa o2) {
            int visibilityResult = Integer.compare(o1.getVisibility().ordinal(), o2.getVisibility().ordinal());
            if (visibilityResult == 0) {
                int occupancyResult = Double.compare(o1.getOccupancy().toInt(), o2.getOccupancy().toInt());
                if (occupancyResult == 0) {
                    return o1.getName().compareTo(o2.getName());
                } else return occupancyResult;
            } else return visibilityResult;
        }
    };

    private static final Comparator<Mensa> lexicographicComparator = new Comparator<Mensa>() {
        @Override
        public int compare(Mensa o1, Mensa o2) {
            int visibilityResult = Integer.compare(o1.getVisibility().ordinal(), o2.getVisibility().ordinal());
            if (visibilityResult == 0) {
                return o1.getName().compareTo(o2.getName());
            } else return visibilityResult;
        }
    };

    static LinkedList<Mensa> sortMensaData(LinkedList<Mensa> items, MensaListModel.SortingStrategy sortingStrategy) {
        Comparator<Mensa> comparator;
        switch(sortingStrategy){
            case DISTANCE:
                comparator = distanceComparator; break;
            case OCCUPANCY:
                comparator = occupancyComparator; break;
            case ALPHABETICALLY:
                comparator = lexicographicComparator; break;
            default:
                comparator = lexicographicComparator;
        }
        Collections.sort(items, comparator);
        return items;

    }


}
