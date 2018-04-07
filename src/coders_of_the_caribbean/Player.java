//package coders_of_the_caribbean;

import java.util.*;
import java.io.*;
import java.math.*;

class Player {

    private static ArrayList<String> ACTIONS = new ArrayList<>(Arrays.asList(new String[]{"WAIT", "SLOWER", "FASTER", "STARBOARD", "PORT", "MINE", "FIRE"}));
    private static ArrayList<Ship> allyShips = new ArrayList<>();
    private static ArrayList<Ship> enemyShips = new ArrayList<>();
    private static ArrayList<Barrel> barrels = new ArrayList<>();
    private static ArrayList<Mine> mines = new ArrayList<>();
    private static ArrayList<Cannonball> cannonballs = new ArrayList<>();
    private static ArrayList<Hex> mapLimits = new ArrayList<>();
    private static ArrayList<String> lastActions = new ArrayList<>();
    private static ArrayList<Integer> turnsToMine = new ArrayList<>();
    private static Ship currentShip = null;
    private static Ship enemyTarget = null;
    private static Barrel barrelTarget = null;
    private static Mine mineTarget = null;
    private static int temperature = 0;

    private static Random random = new Random();

    public static void main(String args[]) {
        Scanner in = new Scanner(System.in);

        for (int x = -1; x < 23; x++) {
            mapLimits.add(new Hex(x, -1));
            mapLimits.add(new Hex(x, 21));
        }
        for (int y = -1; y < 21; y++) {
            mapLimits.add(new Hex(-1, y));
            mapLimits.add(new Hex(23, y));
        }

        //game loop
        while (true) {
            allyShips.clear();
            enemyShips.clear();
            barrels.clear();
            mines.clear();
            cannonballs.clear();
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
                switch (entityType) {
                    case "SHIP":
                        if (arg4 == 1) {
                            allyShips.add(new Ship(entityId, new Hex(x, y), arg1, arg2, arg3, arg4 == 1));
                        } else if (arg4 == 0) {
                            enemyShips.add(new Ship(entityId, new Hex(x, y), arg1, arg2, arg3, arg4 == 1));
                        }
                        break;
                    case "BARREL":
                        barrels.add(new Barrel(entityId, new Hex(x, y), arg1));
                        break;
                    case "MINE":
                        mines.add(new Mine(entityId, new Hex(x, y)));
                        break;
                    case "CANNONBALL":
                        if (arg2 == 1) {
                            cannonballs.add(new Cannonball(entityId, new Hex(x, y), arg1, arg2));
                        }
                        break;
                    default:
                        break;
                }
            }

            for (int i = 0; i < myShipCount; i++) {

                //el barco actual
                currentShip = allyShips.get(i);

                //para obtener el mejor barril a buscar
                barrelTarget = getBestBarrell();

                //para obtener el enemigo mas cercano
                enemyTarget = getCloseEnemy();

                //para obtener la mina mas cercana
                mineTarget = getCloseMine();

                /*if (i == 0) {
                    if (barrelTarget != null) {
                        System.err.println("D/ barrel: " + currentShip.getPosition().distanceTo(barrelTarget.getPosition()));
                    }

                    if (enemyTarget != null) {
                        System.err.println("D/ enemy: " + currentShip.getPosition().distanceTo(enemyTarget.getPosition()));
                    }

                    if (mineTarget != null) {
                        System.err.println("D/ mine: " + currentShip.getPosition().distanceTo(mineTarget.getPosition()));
                    }
                }*/
                //para inicializar turnsToMine
                try {
                    turnsToMine.get(i);
                } catch (IndexOutOfBoundsException e) {
                    turnsToMine.add(0);
                }

                //para inicializar lastActions
                try {
                    lastActions.get(i);
                } catch (IndexOutOfBoundsException e) {
                    lastActions.add("WAIT");
                }

                String bestAction;

                //bestAction = hillClimbing();
                bestAction = simulatedAnnealing();

                //imprimo la accion que obtuvo mayor valor segun la estrategia
                doAction(bestAction);

                //seteo la ultima accion hecha por el barco
                lastActions.set(i, bestAction);

                //para el caso que no use la accion MINE
                if (turnsToMine.get(i) > 0) {
                    turnsToMine.set(i, turnsToMine.get(i) - 1);
                }
                //en caso de usar la accion MINE
                if (bestAction == "MINE") {
                    turnsToMine.set(i, 4);
                }

                /*if (i == 0) {
                    System.err.println("");
                    System.err.println("ACTION : " + bestAction);
                    System.err.println("POSITION : "
                            + "(" + allyShips.get(i).getPosition().getX()
                            + "," + allyShips.get(i).getPosition().getY() + ") -> "
                            + "(" + allyShips.get(i).getFutureShip(bestAction).getPosition().getX()
                            + "," + allyShips.get(i).getFutureShip(bestAction).getPosition().getY() + ") ->obs "
                            + "(" + allyShips.get(i).getFutureShipWithObstacles(bestAction, mapLimits).getPosition().getX()
                            + "," + allyShips.get(i).getFutureShipWithObstacles(bestAction, mapLimits).getPosition().getY() + ")"
                    );
                    System.err.println("FRONT : "
                            + "(" + allyShips.get(i).getFront().getX()
                            + "," + allyShips.get(i).getFront().getY() + ") -> "
                            + "(" + allyShips.get(i).getFutureShip(bestAction).getFront().getX()
                            + "," + allyShips.get(i).getFutureShip(bestAction).getFront().getY() + ") ->obs "
                            + "(" + allyShips.get(i).getFutureShipWithObstacles(bestAction, mapLimits).getFront().getX()
                            + "," + allyShips.get(i).getFutureShipWithObstacles(bestAction, mapLimits).getFront().getY() + ")"
                    );
                    System.err.println("BACK : "
                            + "(" + allyShips.get(i).getBack().getX()
                            + "," + allyShips.get(i).getBack().getY() + ") -> "
                            + "(" + allyShips.get(i).getFutureShip(bestAction).getBack().getX()
                            + "," + allyShips.get(i).getFutureShip(bestAction).getBack().getY() + ") ->obs "
                            + "(" + allyShips.get(i).getFutureShipWithObstacles(bestAction, mapLimits).getBack().getX()
                            + "," + allyShips.get(i).getFutureShipWithObstacles(bestAction, mapLimits).getBack().getY() + ")"
                    );
                }*/
                //seteo el barco en la posicion en la que quedara luego de realizar la accion
                allyShips.set(i, currentShip.getFutureShipWithObstacles(bestAction, mapLimits));

            }
        }
    }

    public static String hillClimbing() {
        String bestAction = "WAIT";

        int strategyMax = Integer.MIN_VALUE;
        int strategyActual = 0;
        
        //segun una estrategia (funcion de evaluacion), obtengo la accion que la maximice
        for (int a = 0; a < ACTIONS.size(); a++) {
            strategyActual = strategy(ACTIONS.get(a));
            if (strategyActual > strategyMax) {
                strategyMax = strategyActual;
                bestAction = ACTIONS.get(a);
            }
            //if (currentShip.getId() == 0) {
                //System.err.println("(" + ship.getId() + ")" + ACTIONS.get(fs) + " : " + strategyActual);
            //}
        }

        return bestAction;
    }

    public static String simulatedAnnealing() {
        
        String bestAction = "WAIT";
        String iteraAction;
        int deltaE;
        float kmax = 200;
        float T = temperature / kmax;
        
        System.err.println("Temperature : " + (kmax - temperature));
        
        int i = 1;
        boolean actionFound = false;
        while (i < ACTIONS.size() && !actionFound) {
            iteraAction = ACTIONS.get(random.nextInt(7));
            //iteraAction = ACTIONS.get(i);
            // ∆E = value(nextState) - value(currentState)
            deltaE = strategy(iteraAction) - strategy(lastActions.get(allyShips.indexOf(currentShip)));
            if (deltaE > 0) {
                bestAction = iteraAction;
                actionFound = true;
            } else if (Math.pow(Math.E, deltaE / T) > Math.random()) {
                bestAction = iteraAction;
                actionFound = true;
            }
            i++;
        }
        
        if (temperature < kmax) {
            temperature+=10;
        }

        //notifico la accion que haria hill climbing
        notifyAction(hillClimbing());
        return bestAction;
    }

    public static void doAction(String action) {
        currentShip = currentShip.getFutureShip(action);
        if (!action.equals("FIRE")) {
            System.out.println(action + " " + action);
        } else {
            fireAction();
        }
    }

    public static void notifyAction(String action) {
        if (!action.equals("FIRE")) {
            System.err.println("[Hill-Climbing]: " + action);
        } else {
            fireAction();
        }
    }

    public static void fireAction() {        
        //futureEnemyHex = enemyTarget.getPosition().getNeighbor(enemyTarget.getOrientation(),(1 + distanceToEnemy / 3));
        
        int attackDirection = enemyTarget.getOrientation();
        
        int distanceToEnemy = currentShip.getFront().distanceTo(enemyTarget.getPosition());

        Hex futureEnemyHex = enemyTarget.getPosition();
        int j = 0;
        while (j < (1 + distanceToEnemy / 3) * enemyTarget.getSpeed() && !futureEnemyHex.isOutOfMap()) {
            futureEnemyHex = futureEnemyHex.getNeighbor(attackDirection);
            j++;
        }

        //caso para la accion FIRE que requiere coordenadas
        System.out.println("FIRE " + futureEnemyHex.getX() + " " + futureEnemyHex.getY()
                + " " + "FIRE " + futureEnemyHex.getX() + " " + futureEnemyHex.getY());
    }

    public static Barrel getBestBarrell() {
        Barrel barrelTarget = null;
        int distanceToBarrel;
        float barrelHeuristicActual;
        float barrelHeuristicMax = Integer.MIN_VALUE;
        for (Barrel barrel : barrels) {
            distanceToBarrel = currentShip.getFront().distanceTo(barrel.getPosition());
            barrelHeuristicActual = barrel.getRum() / distanceToBarrel;
            if (barrelHeuristicMax < barrelHeuristicActual) {
                barrelTarget = barrel;
                barrelHeuristicMax = barrelHeuristicActual;
            }
        }
        return barrelTarget;
    }

    public static Ship getCloseEnemy() {
        Ship enemyTarget = null;
        int distanceToEnemyActual;
        int distanceToEnemyMin = Integer.MAX_VALUE;
        for (Ship enemyShip : enemyShips) {
            distanceToEnemyActual = currentShip.getFront().distanceTo(enemyShip.getPosition());
            if (distanceToEnemyActual < distanceToEnemyMin) {
                distanceToEnemyMin = distanceToEnemyActual;
                enemyTarget = enemyShip;
            }
        }
        return enemyTarget;
    }

    public static Mine getCloseMine() {
        Mine mineTarget = null;
        int distanceToMineActual;
        int distanceToMineMin = Integer.MAX_VALUE;
        for (Mine mine : mines) {
            distanceToMineActual = currentShip.getFront().distanceTo(mine.getPosition());
            if (distanceToMineActual < distanceToMineMin) {
                distanceToMineMin = distanceToMineActual;
                mineTarget = mine;
            }
        }
        return mineTarget;
    }

    public static int strategy(String action) {
        int value = 100;
        
        Ship neighborShip = currentShip.getFutureShipWithObstacles(action,mapLimits);
        
        int distanceToEnemy = neighborShip.getFront().distanceTo(enemyTarget.getPosition());
        String lastAction = lastActions.get(allyShips.indexOf(neighborShip));

        if (barrelTarget != null) {
            value = value - neighborShip.getFront().distanceTo(barrelTarget.getPosition()) * 2;
        } else {
            value = value - distanceToEnemy * 2;
        }

        if (mineTarget != null) {
            if (neighborShip.getFront().distanceTo(mineTarget.getPosition()) <= 2) {
                value = value + neighborShip.getFront().distanceTo(mineTarget.getPosition());
            }
        }

        if (neighborShip.collide(allyShips) || neighborShip.collide(enemyShips)) {
            value = value - 20;
        }

        if (neighborShip.collide(mines)) {
            //System.err.println(action + " collide with mine");
            value = value - 20;
        }

        if (neighborShip.collide(cannonballs)) {
            value = value - 10;
        }

        if (neighborShip.collide(barrels)) {
            value = value + 5;
        }

        if (action.equals("PORT")) {
            if (neighborShip.getSpeed() == 0) {
                value = value + 5;
            }
        }

        if (action.equals("FIRE")) {
            if (!lastAction.equals("FIRE")) {
                if (neighborShip.getSpeed() > 0) {
                    if (distanceToEnemy <= 10) {
                        value = value + 20;
                    } else if (distanceToEnemy <= 5) {
                        value = value + 10;
                    }
                } else {
                    value = value - 5;
                }
            } else {
                value = value - 20;
            }
        }

        if (action.equals("MINE")) {
            if (turnsToMine.get(allyShips.indexOf(neighborShip)) == 0) {
                if (neighborShip.getSpeed() > 0 && distanceToEnemy > 10) {
                    value = value + 5;
                } else {
                    value = value - 10;
                }
            } else {
                value = value - 20;
            }
        }

        if (action.equals("FASTER")) {
            if (!neighborShip.getFront().equals(mapLimits)) {
                switch (neighborShip.getSpeed()) {
                    case 0:
                        value = value + 5;
                        break;
                    case 1:
                        value = value + 10;
                        break;
                    case 2:
                        value = value - 20;
                        break;
                    default:
                        break;
                }
            } else {
                value = value - 15;
            }
        }

        if (action.equals("SLOWER")) {
            if (neighborShip.getSpeed() == 0) {
                value = value - 10;
            }
        }

        if (action.equals("WAIT")) {
            if (neighborShip.getSpeed() == 0) {
                value = value - 20;
            }
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

    @Override
    public boolean equals(Object obj) {
        Entity otherEntity = (Entity) obj;
        return (this.id == otherEntity.getId());
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
        if (action.equals("SLOWER")) {
            if (speed > 0) {
                futureShip.setSpeed(speed - 1);
            }
        } else if (action.equals("FASTER")) {
            if (speed < 2) {
                futureShip.setSpeed(speed + 1);
            }
        }

        //posicion cambiara segun la velocidad
        futureShip.setPosition(position.getNeighbor(orientation, futureShip.getSpeed()));

        //acciones que cambian la orientacion
        if (action.equals("PORT")) {
            futureShip.setOrientation(getPortDirection());
        } else if (action.equals("STARBOARD")) {
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
        if (action.equals("SLOWER")) {
            if (speed > 0) {
                futureShip.setSpeed(speed - 1);
            }
        } else if (action.equals("FASTER")) {
            if (speed < 2) {
                futureShip.setSpeed(speed + 1);
            }
        }

        //posicion cambiara segun la velocidad
        Hex futurePosition = position;
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
        if (action.equals("PORT")) {
            futureShip.setOrientation(getPortDirection());
        } else if (action.equals("STARBOARD")) {
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
