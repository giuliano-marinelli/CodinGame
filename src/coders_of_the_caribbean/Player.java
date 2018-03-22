package coders_of_the_caribbean;

import java.util.*;
import java.io.*;
import java.math.*;

class Player {

    private static int[][][] oddrDirections
            = {{{1, 0}, {0, -1}, {-1, -1}, {-1, 0}, {-1, 1}, {0, 1}},
            {{1, 0}, {1, -1}, {0, -1}, {-1, 0}, {0, 1}, {1, 1}}};
    private static ArrayList<Ship> allyShips = new ArrayList<>();
    private static ArrayList<Ship> enemyShips = new ArrayList<>();
    private static ArrayList<Barrel> barrels = new ArrayList<>();
    private static ArrayList<Mine> mines = new ArrayList<>();
    private static Barrel barrelTarget = null;
    private static float barrelHeuristicMax = 0;

    public static void main(String args[]) {
        Scanner in = new Scanner(System.in);

        // game loop
        while (true) {
            allyShips.clear();
            enemyShips.clear();
            barrels.clear();
            mines.clear();
            int myShipCount = in.nextInt(); // the number of remaining ships
            int entityCount = in.nextInt(); // the number of entities (e.g. ships, mines or cannonballs)
            for (int i = 0; i < entityCount; i++) {
                int entityId = in.nextInt();
                String entityType = in.next();
                int x = in.nextInt();
                int y = in.nextInt();
                int arg1 = in.nextInt();
                int arg2 = in.nextInt();
                int arg3 = in.nextInt();
                int arg4 = in.nextInt();
                if (entityType.equals("BARREL")) {
                    barrels.add(new Barrel(entityId, x, y, arg1));
                } else if (entityType.equals("SHIP")) {
                    if (arg4 == 1) {
                        allyShips.add(new Ship(entityId, x, y, arg1, arg2, arg3, arg4 == 1));
                    } else if (arg4 == 0) {
                        enemyShips.add(new Ship(entityId, x, y, arg1, arg2, arg3, arg4 == 1));
                    }
                } else if (entityType.equals("MINE")) {
                    mines.add(new Mine(entityId, x, y));
                }
            }
            for (int i = 0; i < myShipCount; i++) {
                barrelTarget = null;
                barrelHeuristicMax = 0;
                for (Barrel barrel : barrels) {
                    int barrelDistance = distance(
                            new int[]{allyShips.get(0).getX(), allyShips.get(0).getY()},
                            new int[]{barrel.getX(), barrel.getY()});
                    int barrelRum = barrel.getRum();
                    float barrelHeuristicActual = barrelRum / barrelDistance;
                    if (barrelHeuristicMax < barrelHeuristicActual) {
                        barrelTarget = barrel;
                        barrelHeuristicMax = barrelHeuristicActual;
                    }
                }
                if (barrelTarget != null) {
                    System.err.println("Distance to [Barrel Target]: " + distance(
                            new int[]{allyShips.get(0).getX(), allyShips.get(0).getY()},
                            new int[]{barrelTarget.getX(), barrelTarget.getY()}));
                    System.err.println("Barrel Heuristic: " + barrelHeuristicMax);
                    System.out.println("MOVE " + barrelTarget.getX() + " " + barrelTarget.getY());
                } else {
                    Random random = new Random();
                    System.out.println("MOVE " + random.nextInt(22) + " " + random.nextInt(20));
                }
            }
        }
    }

    private static int[] oddrOffsetNeighbor(int[] hex, int direction) {
        //devuelve la coordenada de el hexagono vecino en la direccion indicada
        int par = hex[1] % 2;
        int[] dir = oddrDirections[par][direction];
        return new int[]{hex[0] + dir[0], hex[1] + dir[1]};
    }

    private static int[] oddrToCube(int[] hex) {
        //traduce de exagono a cubo
        int[] cube = new int[3];
        cube[0] = hex[0] - (hex[1] - (hex[1] % 2)) / 2;
        cube[2] = hex[1];
        cube[1] = -cube[0] - cube[2];
        return cube;
    }

    private static int[] cubeToOddr(int[] cube) {
        //traduce de cubo a hexaagono
        int[] hex = new int[2];
        hex[0] = cube[0] + (cube[2] - (cube[2] % 2)) / 2;
        hex[1] = cube[2];
        return hex;
    }

    private static int cubeDistance(int[] origen, int[] destino) {
        return (Math.abs(origen[0] - destino[0]) + Math.abs(origen[1] - destino[1]) + Math.abs(origen[2] - destino[2])) / 2;
    }

    private static int distance(int[] origen, int[] destino) {
        //mide la distancia desde origen a destino contando la posicion de origen
        int[] a = oddrToCube(origen);
        int[] b = oddrToCube(destino);
        return cubeDistance(a, b);
    }

}

class Ship {

    private int id;
    private int x;
    private int y;
    private int orientation;
    private int speed;
    private int rum;
    private boolean isAlly;

    public Ship(int id, int x, int y, int orientation, int speed, int rum, boolean isAlly) {
        this.id = id;
        this.x = x;
        this.y = y;
        this.orientation = orientation;
        this.speed = speed;
        this.rum = rum;
        this.isAlly = isAlly;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public int getSpeed() {
        return speed;
    }

    public void setSpeed(int speed) {
        this.speed = speed;
    }

    public int getRum() {
        return rum;
    }

    public void setRum(int rum) {
        this.rum = rum;
    }

    public int getOrientation() {
        return orientation;
    }

    public void setOrientation(int orientation) {
        this.orientation = orientation;
    }

    public boolean getIsAlly() {
        return isAlly;
    }

    public void setIsAlly(boolean isAlly) {
        this.isAlly = isAlly;
    }
}

class Barrel {

    private int id;
    private int x;
    private int y;
    private int rum;

    public Barrel(int id, int x, int y, int rum) {
        this.id = id;
        this.x = x;
        this.y = y;
        this.rum = rum;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public int getRum() {
        return rum;
    }

    public void setRum(int rum) {
        this.rum = rum;
    }
}

class Mine {

    private int id;
    private int x;
    private int y;

    public Mine(int id, int x, int y) {
        this.id = id;
        this.x = x;
        this.y = y;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }
}

class Cannonball {

    private int id;
    private int x;
    private int y;
    private int idShip;
    private int turnsToImpact;

    public Cannonball(int id, int x, int y, int idShip, int turnsToImpact) {
        this.id = id;
        this.x = x;
        this.y = y;
        this.idShip = idShip;
        this.turnsToImpact = turnsToImpact;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public int getIdShip() {
        return idShip;
    }

    public void setIdShip(int idShip) {
        this.idShip = idShip;
    }

    public int getTurnsToImpact() {
        return turnsToImpact;
    }

    public void setTurnsToImpact(int turnsToImpact) {
        this.turnsToImpact = turnsToImpact;
    }
}
