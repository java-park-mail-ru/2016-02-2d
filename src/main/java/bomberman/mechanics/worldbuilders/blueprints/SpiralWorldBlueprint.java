package bomberman.mechanics.worldbuilders.blueprints;

import bomberman.mechanics.interfaces.Blueprintable;

public class SpiralWorldBlueprint extends Blueprintable {

    @Override
    public String[] getBlueprint() {
        return new String[]{
                //123456789ABCDEF
                "###############", //1
                "#...........dS#", //2
                "#...........d.#", //3
                "#.ddddddddd.d.#", //4
                "#.d.......d.d.#", //5
                "#.d.ddddd.d.d.#", //6
                "#.d.d...d.d.d.#", //7
                "#.d.d.dHd.d.d.#", //8
                "#.d.d.d...d.d.#", //9
                "#.d.d.ddddd.d.#", //A
                "#.d.d.......d.#", //B
                "#.d.ddddddddd.#", //C
                "#.d...........#", //D
                "#Sd...........#", //E
                "###############", //F
        };
    }

}
