package com.example.bw4ever.modelo;

import java.util.ArrayList;
import java.util.List;

public class ParqueService {
    public static List<Parque> parqueList = new ArrayList<>();

    public static void addParque(Parque parque){
        parqueList.add(parque);
    }

    public static void removeParque(Parque parque){
        parqueList.remove(parque);
    }

    public static void updateParque(Parque parque){
        parqueList.set(parqueList.indexOf(parque), parque);
    }
}
