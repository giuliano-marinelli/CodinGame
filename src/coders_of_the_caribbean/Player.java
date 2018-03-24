//package coders_of_the_caribbean;

import java.util.*;
import java.io.*;
import java.math.*;

class Player {

    private static String[] ACTIONS = {"WAIT", "SLOWER", "FASTER", "STARBOARD", "PORT", "MINE", "FIRE"};
    private static ArrayList<Ship> allyShips = new ArrayList<>();
    private static ArrayList<Ship> enemyShips = new ArrayList<>();
    private static ArrayList<Barrel> barrels = new ArrayList<>();
    private static ArrayList<Mine> mines = new ArrayList<>();
    private static ArrayList<Cannonball> cannonballs = new ArrayList<>();
    private static ArrayList<Hex> obstacles = new ArrayList<>();
    private static ArrayList<Ship> futureShips = new ArrayList<>();
    private static ArrayList<Ship> enemyTargets = new ArrayList<>();
    private static Ship enemyTarget = null;
    private static int distanceToEnemy = 0;
    private static Barrel barrelTarget = null;
    private static float barrelHeuristicActual = 0;
    private static float barrelHeuristicMax = 0;
    private static Random random = new Random();

    public static void main(String args[]) {
        Scanner in = new Scanner(System.in);

        //game loop
        while (true) {
            allyShips.clear();
            enemyShips.clear();
            barrels.clear();
            mines.clear();
            obstacles.clear();
            futureShips.clear();
            enemyTargets.clear();
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
                        allyShips.add(new Ship(entityId, new Hex(x, y), arg1, arg2, arg3, arg4 == 1));
                    } else if (arg4 == 0) {
                        enemyShips.add(new Ship(entityId, new Hex(x, y), arg1, arg2, arg3, arg4 == 1));
                    }
                    Hex position = new Hex(x, y);
                    obstacles.add(position);
                    obstacles.add(position.getNeighbor(arg1));
                    obstacles.add(position.getNeighbor(Direction.invert(arg1)));
                } else if (entityType.equals("BARREL")) {
                    barrels.add(new Barrel(entityId, new Hex(x, y), arg1));
                } else if (entityType.equals("MINE")) {
                    mines.add(new Mine(entityId, new Hex(x, y)));
                } else if (entityType.equals("CANNONBALL")) {
                    cannonballs.add(new Cannonball(entityId, new Hex(x, y), arg1, arg2));
                }
            }

            for (int x = -1; x < 23; x++) {
                obstacles.add(new Hex(x, -2));
                obstacles.add(new Hex(x, 22));
            }
            for (int y = -1; y < 21; y++) {
                obstacles.add(new Hex(-2, y));
                obstacles.add(new Hex(24, y));
            }

            for (int i = 0; i < myShipCount; i++) {

                futureShips.clear();

                barrelTarget = null;
                barrelHeuristicMax = 0;
                int distanceToBarrel = 0;
                for (Barrel barrel : barrels) {
                    //barrelDistance = hexDistance(
                    //        allyShips.get(i).getPosition(), barrel.getPosition());
                    distanceToBarrel = allyShips.get(i).getFront().distanceTo(barrel.getPosition());
                    barrelHeuristicActual = barrel.getRum() / distanceToBarrel;
                    if (barrelHeuristicMax < barrelHeuristicActual) {
                        barrelTarget = barrel;
                        barrelHeuristicMax = barrelHeuristicActual;
                    }
                }

                enemyTarget = null;
                distanceToEnemy = Integer.MAX_VALUE;
                int distanceToEnemyActual;
                for (Ship enemyShip : enemyShips) {
                    distanceToEnemyActual = allyShips.get(i).getFront().distanceTo(enemyShip.getPosition());
                    if (distanceToEnemyActual < distanceToEnemy) {
                        distanceToEnemy = distanceToEnemyActual;
                        enemyTarget = enemyShip;
                    }
                }

                //obtengo todos los posibles barcos dados cada accion
                for (int a = 0; a < ACTIONS.length; a++) {
                    futureShips.add(allyShips.get(i).getFutureShip(ACTIONS[a]));
                    //futureShips.add(allyShips.get(i).getFutureShipWithObstacles(ACTIONS[a], obstacles));
                }

                //segun una estrategia (funcion de evaluacion), obtengo el barco que la maximice
                int indexMax = 0;
                int strategyMax = Integer.MIN_VALUE;
                int strategyActual = 0;
                for (int fs = 0; fs < futureShips.size(); fs++) {
                    strategyActual = strategy(allyShips.get(i), futureShips.get(fs), ACTIONS[fs]);
                    if (strategyActual > strategyMax) {
                        strategyMax = strategyActual;
                        indexMax = fs;
                    }
                    System.err.println("(" + i + ")" + ACTIONS[fs] + " : " + strategyActual);
                }

                //imprimo la accion que obtuvo mayor valor segun la estrategia
                if (indexMax != 6) {
                    System.out.println(ACTIONS[indexMax]);
                } else {
                    int attackDirection = enemyTarget.getOrientation();
                    if (i > 0 && !enemyTargets.isEmpty() && enemyTargets.contains(enemyTarget)) {
                        attackDirection = enemyTarget.getPortDirection();
                    }
                    Hex futureEnemyHex = enemyTarget.getPosition();
                    int j = 0;
                    while (j < (1 + distanceToEnemy / 3) * enemyTarget.getSpeed() && !futureEnemyHex.isOutOfMap()) {
                        futureEnemyHex = futureEnemyHex.getNeighbor(attackDirection);
                        j++;
                    }
                    enemyTargets.add(enemyTarget);
                    //caso para la accion FIRE que requiere coordenadas
                    System.out.println(ACTIONS[indexMax] + " "
                            + futureEnemyHex.getX() + " " + futureEnemyHex.getY());
                }
//
//                if (barrelTarget != null && ((barrelTarget.getRum() <= (100 - allyShips.get(i).getRum()) && distanceToEnemy > 10)
//                        || allyShips.get(i).getSpeed() == 0)) {
//
//                    //PARA MOVERSE DE A UNA CASILLA
//                    //moveToTarget(i, barrelTarget.getPosition());
//                    //MOVERSE DE FORMA CLASICA
//                    //System.out.println("MOVE " + barrelTarget.getPosition().getX() + " " + barrelTarget.getPosition().getY());
//                    System.err.println("[Barrel Target] = ("
//                            + barrelTarget.getPosition().getX() + ","
//                            + barrelTarget.getPosition().getY() + ")");
//                    System.err.println("Heuristic [Barrel Target] = " + barrelHeuristicMax);
//
//                } else if (enemyTarget != null) {
//                    Hex hexForward = hexOffsetNeighbor(allyShips.get(i).getPosition(), allyShips.get(i).getOrientation());
//                    Hex hexStraightTop = hexOffsetNeighbor(hexForward, allyShips.get(i).getOrientation());
//                    if (distanceToEnemy > 7 || allyShips.get(i).getSpeed() < 1
//                            || (hexStraightTop != null && hexCollide(hexStraightTop, mines))) {
//                        if (barrelTarget != null) {
//                            //moveToTarget(i, barrelTarget.getPosition());
//                        } else {
//                            //moveToTarget(i, enemyTarget.getPosition());
//                        }
//                        //System.out.println("MOVE " + enemyTarget.getPosition().getX() + " " + enemyTarget.getPosition().getY());
//                    } else if (distanceToEnemy <= 7) {
//                        int attackDirection = enemyTarget.getOrientation();
//                        if (i > 0 && !enemyTargets.isEmpty() && enemyTargets.contains(enemyTarget)) {
//                            attackDirection = enemyTarget.getLeftDirection();
//                        }
//                        Hex futureEnemyHex = enemyTarget.getPosition();
//                        int j = 0;
//                        while (j < (1 + distanceToEnemy / 3) * enemyTarget.getSpeed() && !hexBorderOfMap(futureEnemyHex)) {
//                            /*int randomDirection;
//                            switch (random.nextInt(3)) {
//                                case 0:
//                                    randomDirection = enemyTarget.getOrientation();
//                                    break;
//                                case 1:
//                                    randomDirection = enemyTarget.getLeftDirection();
//                                    break;
//                                case 2:
//                                    randomDirection = enemyTarget.getRightDirection();
//                                    break;
//                                default:
//                                    randomDirection = enemyTarget.getOrientation();
//                                    break;
//                            }*/
//                            futureEnemyHex = hexOffsetNeighbor(futureEnemyHex, attackDirection);
//                            j++;
//                        }
//                        System.out.println("FIRE " + futureEnemyHex.getX() + " " + futureEnemyHex.getY());
//                    }
//                    /*else if (distanceToEnemy >= 10) {
//                        System.out.println("MINE");
//                    }*/
//                    System.err.println("Distance [EnemyShip] = " + distanceToEnemy);
//
//                    //MOVIMIENTO ALEATORIO
//                    //System.out.println("MOVE " + random.nextInt(22) + " " + random.nextInt(20));
//                    enemyTargets.add(enemyTarget);
//                }
            }
        }
    }

    public static int strategy(Ship currentShip, Ship neighborShip, String action) {
        int value = 20;

        if (barrelTarget != null) {
            value = value - neighborShip.getFront().distanceTo(barrelTarget.getPosition()) / 2;
        } else {
            value = value - neighborShip.getFront().distanceTo(enemyTarget.getPosition());
        }

        if (neighborShip.collide(allyShips) || neighborShip.collide(enemyShips)) {
            value = value - 5;
        }

        if (neighborShip.collide(mines)) {
            value = value - 10;
        }

        if (neighborShip.collide(barrels)) {
            value = value + 2;
        }

        if (action == "PORT") {
            value = value + 1;
        }

        if (action == "FIRE") {
            if (neighborShip.getSpeed() > 0) {
                if (distanceToEnemy < 5) {
                    value = value + 10;
                } else if (distanceToEnemy < 1) {
                    value = value + 5;
                }
            } else {
                value = value - 5;
            }
        }

        if (action == "MINE") {
            if (neighborShip.getSpeed() > 0 && distanceToEnemy > 10) {
                value = value + 5;
            } else {
                value = value - 10;
            }
        }

        if (action == "FASTER") {
            if (!neighborShip.getFront().equals(obstacles)) {
                if (neighborShip.getSpeed() == 0) {
                    value = value + 5;
                } else if (neighborShip.getSpeed() == 1) {
                    value = value + 2;
                } else if (neighborShip.getSpeed() == 2) {
                    value = value - 10;
                }
            } else {
                value = value - 15;
            }
        }

        if (action == "SLOWER" && neighborShip.getSpeed() == 0) {
            value = value - 10;
        }

        if (action == "WAIT" && neighborShip.getSpeed() == 0) {
            value = value - 10;
        }

        return value;
    }

}

class Entity {

    protected int id;
    protected Hex position;

    public Entity(int id, Hex position) {
        this.id = id;
        this.position = position;
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

    public boolean collide(ArrayList entities) {
        boolean collide = false;
        int i = 0;
        while (i < entities.size() && !collide) {
            if (collide((Entity) entities.get(i))) {
                collide = true;
            }
            i++;
        }
        return collide;
    }

    public boolean collide(Entity entity) {
        return entity.getPosition().equals(position);
    }

}

class Ship extends Entity {

    private Hex front;
    private Hex back;
    private int orientation;
    private int speed;
    private int rum;
    private boolean isAlly;

    public Ship(int id, Hex position, int orientation, int speed, int rum, boolean isAlly) {
        super(id, position);
        this.front = position.getNeighbor(orientation);
        this.back = position.getNeighbor(Direction.invert(orientation));
        this.orientation = orientation;
        this.speed = speed;
        this.rum = rum;
        this.isAlly = isAlly;
    }

    public Hex getFront() {
        return front;
    }

    public void setFront(Hex front) {
        this.front = front;
    }

    public Hex getBack() {
        return back;
    }

    public void setBack(Hex back) {
        this.back = back;
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

    public int getPortDirection() {
        int leftDirection = 0;
        if (orientation != 5) {
            leftDirection = orientation + 1;
        }
        return leftDirection;
    }

    public int getStarboardDirection() {
        int rightDirection = 5;
        if (orientation != 0) {
            rightDirection = orientation - 1;
        }
        return rightDirection;
    }

    @Override
    public boolean collide(Entity entity) {
        return entity.getPosition().equals(position)
                || entity.getPosition().equals(front)
                || entity.getPosition().equals(back);
    }

    public boolean collide(Ship ship) {
        return ship.getPosition().equals(position)
                || ship.getPosition().equals(front)
                || ship.getPosition().equals(back)
                || ship.getFront().equals(position)
                || ship.getFront().equals(front)
                || ship.getFront().equals(back)
                || ship.getBack().equals(position)
                || ship.getBack().equals(front)
                || ship.getBack().equals(back);
    }

    public Ship getFutureShip(String action) {
        //nota: las acciones WAIT, FIRE y MINE no resultarán en cambios en los datos del barco
        Ship futureShip = new Ship(id, position, orientation, speed, rum, isAlly);

        //acciones que cambian la velocidad
        if (action == "SLOWER") {
            if (speed > 0) {
                futureShip.setSpeed(speed - 1);
            }
        } else if (action == "FASTER") {
            if (speed < 2) {
                futureShip.setSpeed(speed + 1);
            }
        }

        //posicion cambiara segun la velocidad
        futureShip.setPosition(position.getNeighbor(orientation, futureShip.getSpeed()));

        //acciones que cambian la orientacion
        if (action == "PORT") {
            futureShip.setOrientation(getPortDirection());
        } else if (action == "STARBOARD") {
            futureShip.setOrientation(getStarboardDirection());
        }

        //frente y atras cambiaran segun la orientacion
        futureShip.setFront(futureShip.getPosition().getNeighbor(futureShip.getOrientation()));
        futureShip.setBack(futureShip.getPosition().getNeighbor(Direction.invert(futureShip.getOrientation())));

        return futureShip;
    }

    public Ship getFutureShipWithObstacles(String action, ArrayList<Hex> obstacles) {
        //nota: las acciones WAIT, FIRE y MINE no resultarán en cambios en los datos del barco
        Ship futureShip = new Ship(id, position, orientation, speed, rum, isAlly);

        //acciones que cambian la velocidad
        if (action == "SLOWER") {
            if (speed > 0) {
                futureShip.setSpeed(speed - 1);
            }
        } else if (action == "FASTER") {
            if (speed < 2) {
                futureShip.setSpeed(speed + 1);
            }
        }

        //posicion cambiara segun la velocidad
        Hex futurePosition = futureShip.getFront();
        int i = 0;
        while (i < futureShip.getSpeed()) {
            if (futurePosition.getNeighbor(orientation).equals(obstacles)) {
                futureShip.setSpeed(0);
            } else {
                futurePosition = futurePosition.getNeighbor(orientation);
            }
            i++;
        }
        futurePosition.getNeighbor(Direction.invert(orientation));
        futureShip.setPosition(futurePosition);

        //acciones que cambian la orientacion
        if (action == "PORT") {
            futureShip.setOrientation(getPortDirection());
        } else if (action == "STARBOARD") {
            futureShip.setOrientation(getStarboardDirection());
        }

        //frente y atras cambiaran segun la orientacion
        if (futurePosition.getNeighbor(futureShip.getOrientation()).equals(obstacles)
                || futurePosition.getNeighbor(Direction.invert(futureShip.getOrientation())).equals(obstacles)) {
            futureShip.setOrientation(orientation);
        }
        futureShip.setFront(futurePosition.getNeighbor(futureShip.getOrientation()));
        futureShip.setBack(futurePosition.getNeighbor(Direction.invert(futureShip.getOrientation())));

        return futureShip;
    }

}

class Barrel extends Entity {

    private int rum;

    public Barrel(int id, Hex position, int rum) {
        super(id, position);
        this.rum = rum;
    }

    public int getRum() {
        return rum;
    }

    public void setRum(int rum) {
        this.rum = rum;
    }
}

class Mine extends Entity {

    public Mine(int id, Hex position) {
        super(id, position);
    }

}

class Cannonball extends Entity {

    private int idShip;
    private int turnsToImpact;

    public Cannonball(int id, Hex position, int idShip, int turnsToImpact) {
        super(id, position);
        this.idShip = idShip;
        this.turnsToImpact = turnsToImpact;
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

    private int[][][] hexDirections
            = {{{1, 0}, {0, -1}, {-1, -1}, {-1, 0}, {-1, 1}, {0, 1}},
            {{1, 0}, {1, -1}, {0, -1}, {-1, 0}, {0, 1}, {1, 1}}};

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

    public boolean isOutOfMap() {
        return (x < 0 || y < 0 || x > 22 || y > 20);
    }

    public boolean isOnBorderOfMap() {
        return (x == 0 || y == 0 || x == 22 || y == 20);
    }

    //devuelve el hexagono vecino en la direccion indicada
    public Hex getNeighbor(int direction) {
        if (isOutOfMap()) {
            return this;
        }
        int even = y % 2;
        int[] dir = hexDirections[even][direction];
        return new Hex(x + dir[0], y + dir[1]);
    }

    //devuelve el hexagono a "distance" casillas en la direccion indicada
    //nota: si distance = 0, entonces es igual al vecino
    public Hex getNeighbor(int direction, int distance) {
        Hex neighbor = this;
        int i = 0;
        while (i < distance && !neighbor.isOutOfMap()) {
            neighbor = neighbor.getNeighbor(direction);
            i++;
        }
        return neighbor;
    }

    //traduce de hexagono a cubo
    public Cube toCube() {
        int cubeX = x - (y - (y % 2)) / 2;
        int cubeZ = y;
        int cubeY = -cubeX - cubeZ;
        Cube cube = new Cube(cubeX, cubeY, cubeZ);
        return cube;
    }

    public int distanceTo(Hex hex) {
        //distancia entre dos hexagonos
        return toCube().distanceTo(hex.toCube());
    }

    public boolean equals(ArrayList<Hex> hexes) {
        boolean equals = false;
        int i = 0;
        while (i < hexes.size() && !equals) {
            if (equals((hexes.get(i)))) {
                equals = true;
            }
            i++;
        }
        return equals;
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

    //traduce de cubo a hexagono
    public Hex toHex() {
        int hexX = x + (z - (z % 2)) / 2;
        int hexY = z;
        Hex hex = new Hex(hexX, hexY);
        return hex;
    }

    //distancia entre dos cubos
    public int distanceTo(Cube cube) {
        return (Math.abs(x - cube.getX()) + Math.abs(y
                - cube.getY()) + Math.abs(z - cube.getZ())) / 2;
    }

}

class Direction {

    public static int invert(int direction) {
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
