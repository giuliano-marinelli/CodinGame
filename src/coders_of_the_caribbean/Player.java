//package coders_of_the_caribbean;

import java.util.*;
import java.io.*;
import java.math.*;

class Player {

    private static int[][][] hexDirections
            = {{{1, 0}, {0, -1}, {-1, -1}, {-1, 0}, {-1, 1}, {0, 1}},
            {{1, 0}, {1, -1}, {0, -1}, {-1, 0}, {0, 1}, {1, 1}}};
    private static ArrayList<Ship> allyShips = new ArrayList<>();
    private static ArrayList<Ship> enemyShips = new ArrayList<>();
    private static ArrayList<Barrel> barrels = new ArrayList<>();
    private static ArrayList<Mine> mines = new ArrayList<>();
    private static ArrayList<Cannonball> cannonballs = new ArrayList<>();
    private static Barrel barrelTarget = null;
    private static float barrelHeuristicActual = 0;
    private static float barrelHeuristicMax = 0;

    public static void main(String args[]) {
        Scanner in = new Scanner(System.in);

        //game loop
        while (true) {
            allyShips.clear();
            enemyShips.clear();
            barrels.clear();
            mines.clear();
            //numero de barcos aliados que quedan
            int myShipCount = in.nextInt();
            //numero de entidades (ships, barrels, mines, cannonballs)
            int entityCount = in.nextInt();
            for (int i = 0; i < entityCount; i++) {
                int entityId = in.nextInt();
                String entityType = in.next();
                int x = in.nextInt();
                int y = in.nextInt();
                int arg1 = in.nextInt();
                int arg2 = in.nextInt();
                int arg3 = in.nextInt();
                int arg4 = in.nextInt();
                if (entityType.equals("SHIP")) {
                    if (arg4 == 1) {
                        allyShips.add(new Ship(entityId, x, y, arg1, arg2, arg3, arg4 == 1));
                    } else if (arg4 == 0) {
                        enemyShips.add(new Ship(entityId, x, y, arg1, arg2, arg3, arg4 == 1));
                    }
                } else if (entityType.equals("BARREL")) {
                    barrels.add(new Barrel(entityId, x, y, arg1));
                } else if (entityType.equals("MINE")) {
                    mines.add(new Mine(entityId, x, y));
                } else if (entityType.equals("CANNONBALL")) {
                    cannonballs.add(new Cannonball(entityId, x, y, arg1, arg2));
                }
            }
            for (int i = 0; i < myShipCount; i++) {
                barrelTarget = null;
                barrelHeuristicMax = 0;
                int barrelDistance = 0;
                int barrelRum = 0;
                for (Barrel barrel : barrels) {
                    //barrelDistance = hexDistance(
                    //        allyShips.get(i).getPosition(), barrel.getPosition());
                    barrelDistance = hexDistance(
                            hexOffsetNeighbor(allyShips.get(i).getPosition(), allyShips.get(i).getOrientation()),
                            barrel.getPosition());
                    barrelRum = barrel.getRum();
                    barrelHeuristicActual = barrelRum / barrelDistance;
                    if (barrelHeuristicMax < barrelHeuristicActual) {
                        barrelTarget = barrel;
                        barrelHeuristicMax = barrelHeuristicActual;
                    }
                }

                int distanceToEnemy = hexDistance(allyShips.get(i).getPosition(), enemyShips.get(i).getPosition());

                if ((barrelTarget != null && barrelTarget.getRum() <= (100 - allyShips.get(i).getRum()) && distanceToEnemy > 7)
                        || allyShips.get(i).getSpeed() == 0) {
                    //PARA MOVERSE DE A UNA CASILLA
                    /*Hex hexForward = hexOffsetNeighbor(allyShips.get(i).getPosition(), allyShips.get(i).getOrientation());
                    Hex hexStraight = hexOffsetNeighbor(hexForward, allyShips.get(i).getOrientation());
                    int distanceStraight = hexDistance(
                            hexStraight,
                            barrelTarget.getPosition());
                    Hex hexTurnLeft = hexOffsetNeighbor(hexForward, allyShips.get(i).getLeftDirection());
                    int distanceTurnLeft = hexDistance(
                            hexTurnLeft,
                            barrelTarget.getPosition());
                    Hex hexTurnRight = hexOffsetNeighbor(hexForward, allyShips.get(i).getRightDirection());
                    int distanceTurnRight = hexDistance(
                            hexTurnRight,
                            barrelTarget.getPosition());
                    if (!hexCollideMine(hexStraight) && !hexCollideEnemyShip(hexStraight) && distanceStraight < distanceTurnLeft && distanceStraight < distanceTurnRight) {
                        if (allyShips.get(i).getSpeed() > 0) {
                            System.out.println("WAIT");
                        } else {
                            System.out.println("MOVE " + hexStraight.getX() + " " + hexStraight.getY());
                        }
                    } else if (!hexCollideMine(hexTurnLeft) && !hexCollideEnemyShip(hexTurnLeft) && distanceTurnLeft <= distanceStraight && distanceTurnLeft <= distanceTurnRight) {
                        System.out.println("MOVE " + hexTurnLeft.getX() + " " + hexTurnLeft.getY());
                    } else if (!hexCollideMine(hexTurnRight) && !hexCollideEnemyShip(hexTurnRight) && distanceTurnRight <= distanceStraight && distanceTurnRight <= distanceTurnLeft) {
                        System.out.println("MOVE " + hexTurnRight.getX() + " " + hexTurnRight.getY());
                    }*/

                    //MOVERSE DE FORMA CLASICA
                    System.out.println("MOVE " + barrelTarget.getPosition().getX() + " " + barrelTarget.getPosition().getY());

                    //System.err.println("[Ship] = (" + hexForward.getX() + "," + hexForward.getY() + ")");
                    System.err.println("[Barrel Target] = ("
                            + barrelTarget.getPosition().getX() + ","
                            + barrelTarget.getPosition().getY() + ")");
                    System.err.println("Heuristic [Barrel Target] = " + barrelHeuristicMax);
                    //System.err.println("Distance [Ship] -> [Barrel Target] = "
                    //        + hexDistance(hexForward, barrelTarget.getPosition()));
                    //System.err.println("[HexS] = (" + hexStraight.getX() + "," + hexStraight.getY() + ")");
                    //System.err.println("[HexTL] = (" + hexTurnLeft.getX() + "," + hexTurnLeft.getY() + ")");
                    //System.err.println("[HexTR] = (" + hexTurnRight.getX() + "," + hexTurnRight.getY() + ")");
                    //System.err.println("Distance [Straight|Left|Right]: "
                    //        + distanceStraight + ", " + distanceTurnLeft + ", " + distanceTurnRight);
                } else {
                    //if (allyShips.get(i).getRum() < enemyShips.get(i).getRum()) {
                    if (distanceToEnemy > 5 || allyShips.get(i).getSpeed() == 0) {
                        System.out.println("MOVE " + enemyShips.get(i).getPosition().getX() + " " + enemyShips.get(i).getPosition().getY());
                    } else if (distanceToEnemy <= 5) {
                        Hex futureEnemyHex = enemyShips.get(i).getPosition();
                        for (int j = 0; j < (1 + distanceToEnemy / 3) * enemyShips.get(i).getSpeed(); j++) {
                            futureEnemyHex = hexOffsetNeighbor(futureEnemyHex, enemyShips.get(i).getOrientation());
                        }
                        System.out.println("FIRE " + futureEnemyHex.getX() + " " + futureEnemyHex.getY());
                    }
                    //} else {

                    //}
                    System.err.println("Distance [EnemyShip] = " + distanceToEnemy);

                    //MOVIMIENTO ALEATORIO
                    //Random random = new Random();
                    //System.out.println("MOVE " + random.nextInt(22) + " " + random.nextInt(20));
                }
            }
        }
    }

    //OLD
    /*private static int[] oddrOffsetNeighbor(int[] hex, int direction) {
        //devuelve la coordenada de el hexagono vecino en la direccion indicada
        int par = hex[1] % 2;
        int[] dir = oddrDirections[par][direction];
        return new int[]{hex[0] + dir[0], hex[1] + dir[1]};
    }

    private static int[] oddrToCube(int[] hex) {
        //traduce de hexagono a cubo
        int[] cube = new int[3];
        cube[0] = hex[0] - (hex[1] - (hex[1] % 2)) / 2;
        cube[2] = hex[1];
        cube[1] = -cube[0] - cube[2];
        return cube;
    }

    private static int[] cubeToOddr(int[] cube) {
        //traduce de cubo a hexagono
        int[] hex = new int[2];
        hex[0] = cube[0] + (cube[2] - (cube[2] % 2)) / 2;
        hex[1] = cube[2];
        return hex;
    }*/
    private static Hex hexOffsetNeighbor(Hex hex, int direction) {
        //devuelve la coordenada de el hexagono vecino en la direccion indicada
        int even = hex.getY() % 2;
        int[] dir = hexDirections[even][direction];
        return new Hex(hex.getX() + dir[0], hex.getY() + dir[1]);
    }

    private static Hex cubeToHex(Cube cube) {
        //traduce de cubo a hexagono
        int cubeX = cube.getX();
        int cubeZ = cube.getZ();
        int hexX = cubeX + (cubeZ - (cubeZ % 2)) / 2;
        int hexY = cubeZ;
        Hex hex = new Hex(hexX, hexY);
        return hex;
    }

    private static Cube hexToCube(Hex hex) {
        //traduce de hexagono a cubo
        int hexX = hex.getX();
        int hexY = hex.getY();
        int cubeX = hexX - (hexY - (hexY % 2)) / 2;
        int cubeZ = hexY;
        int cubeY = -cubeX - cubeZ;
        Cube cube = new Cube(cubeX, cubeY, cubeZ);
        return cube;
    }

    private static int cubeDistance(Cube cubeA, Cube cubeB) {
        //distancia entre dos cubos
        return (Math.abs(cubeA.getX() - cubeB.getX()) + Math.abs(cubeA.getY()
                - cubeB.getY()) + Math.abs(cubeA.getZ() - cubeB.getZ())) / 2;
    }

    private static int hexDistance(Hex hexA, Hex hexB) {
        //distancia entre dos hexagonos
        Cube cubeA = hexToCube(hexA);
        Cube cubeB = hexToCube(hexB);
        return cubeDistance(cubeA, cubeB);
    }

    private static boolean hexCollideMine(Hex hex) {
        boolean collide = false;
        int i = 0;
        while (i < mines.size() && collide == false) {
            if (mines.get(i).getPosition().equals(hex)) {
                collide = true;
            }
            i++;
        }
        return collide;
    }

    private static boolean hexCollideEnemyShip(Hex hex) {
        boolean collide = false;
        int i = 0;
        while (i < enemyShips.size() && collide == false) {
            if (enemyShips.get(i).getPosition().equals(hex)) {
                collide = true;
            }
            i++;
        }
        return collide;
    }

    private static boolean hexCollideAllyShip(Hex hex) {
        boolean collide = false;
        int i = 0;
        while (i < allyShips.size() && collide == false) {
            if (allyShips.get(i).getPosition().equals(hex)) {
                collide = true;
            }
            i++;
        }
        return collide;
    }

}

class Ship {

    private int id;
    private Hex position;
    private int orientation;
    private int speed;
    private int rum;
    private boolean isAlly;

    public Ship(int id, int x, int y, int orientation, int speed, int rum, boolean isAlly) {
        this.id = id;
        this.position = new Hex(x, y);
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

    public Hex getPosition() {
        return position;
    }

    public void setPosition(Hex position) {
        this.position = position;
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

    public int getLeftDirection() {
        int leftDirection = 0;
        if (orientation != 5) {
            leftDirection = orientation + 1;
        }
        return leftDirection;
    }

    public int getRightDirection() {
        int rightDirection = 5;
        if (orientation != 0) {
            rightDirection = orientation - 1;
        }
        return rightDirection;
    }
}

class Barrel {

    private int id;
    private Hex position;
    private int rum;

    public Barrel(int id, int x, int y, int rum) {
        this.id = id;
        this.position = new Hex(x, y);
        this.rum = rum;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Hex getPosition() {
        return position;
    }

    public void setPosition(Hex position) {
        this.position = position;
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
    private Hex position;

    public Mine(int id, int x, int y) {
        this.id = id;
        this.position = new Hex(x, y);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Hex getPosition() {
        return position;
    }

    public void setPosition(Hex position) {
        this.position = position;
    }
}

class Cannonball {

    private int id;
    private Hex position;
    private int idShip;
    private int turnsToImpact;

    public Cannonball(int id, int x, int y, int idShip, int turnsToImpact) {
        this.id = id;
        this.position = new Hex(x, y);
        this.idShip = idShip;
        this.turnsToImpact = turnsToImpact;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Hex getPosition() {
        return position;
    }

    public void setPosition(Hex position) {
        this.position = position;
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

class Hex {

    private int x;
    private int y;

    public Hex(int x, int y) {
        this.x = x;
        this.y = y;
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

class Cube {

    private int x;
    private int y;
    private int z;

    public Cube(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
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

    public int getZ() {
        return z;
    }

    public void setZ(int z) {
        this.z = z;
    }
}
