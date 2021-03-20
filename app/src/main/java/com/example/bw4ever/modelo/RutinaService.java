package com.example.bw4ever.modelo;

import java.util.ArrayList;
import java.util.List;

public class RutinaService {
    public static List<Rutina> rutinaList = new ArrayList<>();

    public static void addRutina(Rutina rutina){
        rutinaList.add(rutina);
    }

    public static void removeRutina(Rutina rutina){
        rutinaList.remove(rutina);
    }

    public static void updateRutina(Rutina rutina){
        rutinaList.set(rutinaList.indexOf(rutina), rutina);
    }
}
