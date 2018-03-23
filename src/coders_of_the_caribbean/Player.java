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
    private static Barrel barrelTargetMax = null;
    private static Barrel barrelTargetMin = null;
    private static float barrelHeuristicActual = 0;
    private static float barrelHeuristicMax = 0;
    private static float barrelHeuristicMin = 0;
    private static Random random = new Random();

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

            ArrayList<Ship> enemyTargets = new ArrayList<>();

            for (int i = 0; i < myShipCount; i++) {
                barrelTargetMax = null;
                barrelTargetMin = null;
                barrelHeuristicMax = 0;
                barrelHeuristicMin = Integer.MAX_VALUE;
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
                        barrelTargetMax = barrel;
                        barrelHeuristicMax = barrelHeuristicActual;
                    }
                    barrelHeuristicActual = barrelDistance / barrelRum;
                    if (barrelHeuristicMin > barrelHeuristicActual) {
                        barrelTargetMin = barrel;
                        barrelHeuristicMin = barrelHeuristicActual;
                    }
                }

                Ship enemyTarget = null;
                int distanceToEnemy = Integer.MAX_VALUE;
                int distanceToEnemyActual;
                for (Ship enemyShip : enemyShips) {
                    distanceToEnemyActual = hexDistance(allyShips.get(i).getPosition(), enemyShip.getPosition());
                    if (distanceToEnemyActual < distanceToEnemy) {
                        distanceToEnemy = distanceToEnemyActual;
                        enemyTarget = enemyShip;
                    }
                }

                if (barrelTargetMax != null && ((barrelTargetMax.getRum() <= (100 - allyShips.get(i).getRum()) && distanceToEnemy > 10)
                        || allyShips.get(i).getSpeed() == 0)) {

                    //PARA MOVERSE DE A UNA CASILLA
                    moveToTarget(i, barrelTargetMax.getPosition());

                    //MOVERSE DE FORMA CLASICA
                    //System.out.println("MOVE " + barrelTarget.getPosition().getX() + " " + barrelTarget.getPosition().getY());
                    System.err.println("[Barrel Target] = ("
                            + barrelTargetMax.getPosition().getX() + ","
                            + barrelTargetMax.getPosition().getY() + ")");
                    System.err.println("Heuristic [Barrel Target] = " + barrelHeuristicMax);

                } else if (enemyTarget != null) {
                    Hex hexForward = hexOffsetNeighbor(allyShips.get(i).getPosition(), allyShips.get(i).getOrientation());
                    Hex hexStraightTop = hexOffsetNeighbor(hexForward, allyShips.get(i).getOrientation());
                    if (distanceToEnemy > 7 || allyShips.get(i).getSpeed() < 1
                            || (hexStraightTop != null && hexCollideMine(hexStraightTop))) {
                        if (barrelTargetMax != null) {
                            moveToTarget(i, barrelTargetMax.getPosition());
                        } else {
                            moveToTarget(i, enemyTarget.getPosition());
                        }
                        //System.out.println("MOVE " + enemyTarget.getPosition().getX() + " " + enemyTarget.getPosition().getY());
                    } else if (distanceToEnemy <= 7) {
                        int attackDirection = enemyTarget.getOrientation();
                        if (i > 0 && !enemyTargets.isEmpty() && enemyTargets.contains(enemyTarget)) {
                            attackDirection =  enemyTarget.getLeftDirection();
                        }
                        Hex futureEnemyHex = enemyTarget.getPosition();
                        int j = 0;
                        while (j < (1 + distanceToEnemy / 3) * enemyTarget.getSpeed() && !hexBorderOfMap(futureEnemyHex)) {
                            /*int randomDirection;
                            switch (random.nextInt(3)) {
                                case 0:
                                    randomDirection = enemyTarget.getOrientation();
                                    break;
                                case 1:
                                    randomDirection = enemyTarget.getLeftDirection();
                                    break;
                                case 2:
                                    randomDirection = enemyTarget.getRightDirection();
                                    break;
                                default:
                                    randomDirection = enemyTarget.getOrientation();
                                    break;
                            }*/
                            futureEnemyHex = hexOffsetNeighbor(futureEnemyHex, attackDirection);
                            j++;
                        }
                        System.out.println("FIRE " + futureEnemyHex.getX() + " " + futureEnemyHex.getY());
                    }
                    /*else if (distanceToEnemy >= 10) {
                        System.out.println("MINE");
                    }*/
                    System.err.println("Distance [EnemyShip] = " + distanceToEnemy);

                    //MOVIMIENTO ALEATORIO
                    //System.out.println("MOVE " + random.nextInt(22) + " " + random.nextInt(20));
                    enemyTargets.add(enemyTarget);
                }
            }
        }
    }

    private static void moveToTarget(int shipId, Hex target) {
        Hex hexForward = hexOffsetNeighbor(allyShips.get(shipId).getPosition(), allyShips.get(shipId).getOrientation());

        Hex hexLeftTop = hexOffsetNeighbor(allyShips.get(shipId).getPosition(), allyShips.get(shipId).getLeftDirection());
        Hex hexLeftMid = null;
        Hex hexLeftBot = null;
        if (!hexBorderOfMap(hexLeftTop)) {
            hexLeftMid = hexOffsetNeighbor(hexLeftTop, getOppositeDirection(allyShips.get(shipId).getLeftDirection()));
            hexLeftBot = hexOffsetNeighbor(hexLeftMid, getOppositeDirection(allyShips.get(shipId).getLeftDirection()));
        }

        Hex hexRightTop = hexOffsetNeighbor(allyShips.get(shipId).getPosition(), allyShips.get(shipId).getRightDirection());
        Hex hexRightMid = null;
        Hex hexRightBot = null;
        if (!hexBorderOfMap(hexRightTop)) {
            hexRightMid = hexOffsetNeighbor(hexRightTop, getOppositeDirection(allyShips.get(shipId).getRightDirection()));
            hexRightBot = hexOffsetNeighbor(hexRightMid, getOppositeDirection(allyShips.get(shipId).getRightDirection()));
        }

        Hex hexStraightTop = hexOffsetNeighbor(hexForward, allyShips.get(shipId).getOrientation());
        //Hex hexStraightMid = hexOffsetNeighbor(hexStraightTop, getOppositeDirection(allyShips.get(shipId).getOrientation()));
        //Hex hexStraightBot = hexOffsetNeighbor(hexStraightMid, getOppositeDirection(allyShips.get(shipId).getOrientation()));

        Hex hexTurnLeftTop = hexOffsetNeighbor(hexForward, allyShips.get(shipId).getLeftDirection());
        Hex hexTurnLeftMid = null;
        Hex hexTurnLeftBot = null;
        if (!hexBorderOfMap(hexTurnLeftTop)) {
            hexTurnLeftMid = hexOffsetNeighbor(hexTurnLeftTop, getOppositeDirection(allyShips.get(shipId).getLeftDirection()));
            hexTurnLeftBot = hexOffsetNeighbor(hexTurnLeftMid, getOppositeDirection(allyShips.get(shipId).getLeftDirection()));
        }

        Hex hexTurnRightTop = hexOffsetNeighbor(hexForward, allyShips.get(shipId).getRightDirection());
        Hex hexTurnRightMid = null;
        Hex hexTurnRightBot = null;
        if (!hexBorderOfMap(hexTurnRightTop)) {
            hexTurnRightMid = hexOffsetNeighbor(hexTurnRightTop, getOppositeDirection(allyShips.get(shipId).getRightDirection()));
            hexTurnRightBot = hexOffsetNeighbor(hexTurnRightMid, getOppositeDirection(allyShips.get(shipId).getRightDirection()));
        }

        int distanceStraight = hexDistance(hexStraightTop, target);
        int distanceTurnLeft = hexDistance(hexTurnLeftTop, target);
        int distanceTurnRight = hexDistance(hexTurnRightTop, target);

        if (allyShips.get(shipId).getSpeed() < 1) {
            if (!hexOutOfMap(hexStraightTop)
                    && !hexCollideMine(hexStraightTop) //&& !hexCollideMine(hexStraightMid) && !hexCollideMine(hexStraightBot)
                    && !hexCollideEnemyShip(hexStraightTop)) {
                System.out.println("FASTER");
                //System.err.println("[Hex Straight] = (" + hexStraightTop.getX() + "," + hexStraightTop.getY() + ")");
            } else if (!hexOutOfMap(hexLeftTop) && hexLeftMid != null && hexLeftBot != null
                    && !hexCollideMine(hexLeftTop) && !hexCollideMine(hexLeftMid) && !hexCollideMine(hexLeftBot)
                    && !hexCollideEnemyShip(hexLeftTop) && distanceTurnLeft <= distanceTurnRight) {
                System.out.println("PORT");
            } else if (!hexOutOfMap(hexRightTop) && hexRightMid != null && hexRightBot != null
                    && !hexCollideMine(hexRightTop) && !hexCollideMine(hexRightMid) && !hexCollideMine(hexRightBot)
                    && !hexCollideEnemyShip(hexRightTop) && distanceTurnRight <= distanceTurnLeft) {
                System.out.println("STARBOARD");
            } else if (!hexOutOfMap(hexLeftTop) && hexLeftMid != null && hexLeftBot != null
                    && !hexCollideMine(hexLeftTop) && !hexCollideMine(hexLeftMid) && !hexCollideMine(hexLeftBot)
                    && !hexCollideEnemyShip(hexLeftTop)) {
                System.out.println("PORT");
            } else {
                System.out.println("STARBOARD");
            }
        } else {
            if (!hexOutOfMap(hexStraightTop)
                    && !hexCollideMine(hexStraightTop) //&& !hexCollideMine(hexStraightMid) && !hexCollideMine(hexStraightBot)
                    && !hexCollideEnemyShip(hexStraightTop) && distanceStraight < distanceTurnLeft && distanceStraight < distanceTurnRight) {
                //System.out.println("MOVE " + hexStraight.getX() + " " + hexStraight.getY());
                System.out.println("WAIT");
            } else if (!hexOutOfMap(hexLeftTop) && hexTurnLeftMid != null && hexTurnLeftBot != null
                    && !hexCollideMine(hexTurnLeftTop) && !hexCollideMine(hexTurnLeftMid) && !hexCollideMine(hexTurnLeftBot)
                    && !hexCollideEnemyShip(hexTurnLeftTop) && distanceTurnLeft <= distanceStraight && distanceTurnLeft <= distanceTurnRight) {
                //System.out.println("MOVE " + hexTurnLeft.getX() + " " + hexTurnLeft.getY());
                System.out.println("PORT");
            } else if (!hexOutOfMap(hexRightTop) && hexTurnRightMid != null && hexTurnRightBot != null
                    && !hexCollideMine(hexTurnRightTop) && !hexCollideMine(hexTurnRightMid) && !hexCollideMine(hexTurnRightBot)
                    && !hexCollideEnemyShip(hexTurnRightTop) && distanceTurnRight <= distanceStraight && distanceTurnRight <= distanceTurnLeft) {
                //System.out.println("MOVE " + hexTurnRight.getX() + " " + hexTurnRight.getY());
                System.out.println("STARBOARD");
            } else {
                System.out.println("WAIT");
            }
        }
        /*System.err.println("[Ship] = (" + hexForward.getX() + "," + hexForward.getY() + ")");
        System.err.println("Distance [Ship] -> [Barrel Target] = "
                + hexDistance(hexForward, target));
        System.err.println("[HexS] = (" + hexStraightTop.getX() + "," + hexStraightTop.getY() + ")" + !hexCollideMine(hexStraightTop));
        System.err.println("[HexTL] = (" + hexTurnLeftTop.getX() + "," + hexTurnLeftTop.getY() + ")" + !hexCollideMine(hexTurnLeftTop));
        System.err.println("[HexTR] = (" + hexTurnRightTop.getX() + "," + hexTurnRightTop.getY() + ")" + !hexCollideMine(hexTurnRightTop));
        System.err.println("Distance [Straight|Left|Right]: "
                + distanceStraight + ", " + distanceTurnLeft + ", " + distanceTurnRight);*/
    }

    private static Hex hexOffsetNeighbor(Hex hex, int direction) {
        //devuelve la coordenada de el hexagono vecino en la direccion indicada
        if (hexOutOfMap(hex)) {
            return hex;
        }
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

    private static boolean hexOutOfMap(Hex hex) {
        boolean outOfMap = false;
        if (hex.getX() < 0 || hex.getY() < 0 || hex.getX() > 22 || hex.getY() > 20) {
            outOfMap = true;
        }
        return outOfMap;
    }

    private static boolean hexBorderOfMap(Hex hex) {
        boolean outOfMap = false;
        if (hex.getX() < 1 || hex.getY() < 1 || hex.getX() > 21 || hex.getY() > 19) {
            outOfMap = true;
        }
        return outOfMap;
    }

    private static int getOppositeDirection(int direction) {
        int oppositeDirection;
        switch (direction) {
            case 0:
                oppositeDirection = 3;
                break;
            case 1:
                oppositeDirection = 4;
                break;
            case 2:
                oppositeDirection = 5;
                break;
            case 3:
                oppositeDirection = 0;
                break;
            case 4:
                oppositeDirection = 1;
                break;
            case 5:
                oppositeDirection = 2;
                break;
            default:
                oppositeDirection = direction;
                break;
        }
        return oppositeDirection;
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

    @Override
    public boolean equals(Object obj) {
        Hex otherHex = (Hex) obj;
        return (this.x == otherHex.getX() && this.y == otherHex.getY());
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
