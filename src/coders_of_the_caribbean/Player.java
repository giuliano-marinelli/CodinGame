
import java.util.*;
import java.io.*;
import java.math.*;

class Player {

    private static ArrayList<Ship> allyShips = new ArrayList<>();
    private static ArrayList<Ship> enemyShips = new ArrayList<>();
    private static ArrayList<Barrel> barrels = new ArrayList<>();
    private static ArrayList<Mine> mines = new ArrayList<>();
    private static ArrayList<Cannonball> cannonballs = new ArrayList<>();
    private static Barrel barrelTargetMax = null;
    private static Barrel barrelTargetMin = null;

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

            for (int i = 0; i < allyShips.size(); i++) {
                allyShips.get(i).buscarMovimiento(allyShips, enemyShips, barrels, mines);
            }
        }
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

    public void buscarMovimiento(ArrayList<Ship> allyShips, ArrayList<Ship> enemyShips,
            ArrayList<Barrel> barrels, ArrayList<Mine> mines) {
        Barrel barrelTargetMax = null;

        /*ACCIONES*/
 /*INSERTE AQUI SU ESTRATEGIA*/
        if (rum > 80) {
            System.err.println("[primer estrategia]");
            /*se acerca al mejor rum pero no lo agarra.*/
            barrelTargetMax = buscarMejorBarril(barrels);
            goBarrel(barrelTargetMax, searchShortEnemy(enemyShips), enemyShips, barrels, mines);
        } else if (rum > 60) {
            System.err.println("[segunda estrategia]");
            /*toma el rum.*/
            barrelTargetMax = buscarMejorBarril(barrels);
            tomarRum(barrelTargetMax, enemyShips, barrels, mines);
        } else {
            System.err.println("[tercera estrategia]");
            /*alejarse del enemigo en busca del Rum que maximice el rum/distanciaEnemigo*/
            Ship enemyTarget = searchShortEnemy(enemyShips);
            barrelTargetMax = buscarMejorBarril(barrels, enemyTarget);
            alejarseEnemigo(barrelTargetMax, enemyTarget, enemyShips, barrels, mines);
        }

    }

    /**
     * Se acerca al barril pero no lo toma. Si estaba en movimiento hacia el
     * barril puede disparar
     */
    private void goBarrel(Barrel bar, Ship enemyTarget, ArrayList<Ship> enemyShips,
            ArrayList<Barrel> barrels, ArrayList<Mine> mines) {

        /*SUPUESTAMENTE SE TIENE QUE MOVER AL LADO DEL BARRIL, NO LLEGAR AL BARRIL.
            SERIA?????  
            position.moveToTarget(this, new Hex(bar.getPosition().getX() - 1, bar.getPosition().getY() - 1), enemyShips, barrels, mines);
         */
        position.moveToTarget(this, bar.getPosition(), enemyShips, barrels, mines);
    }

    /**
     * toma el Rum del barril que esté mas cerca. Si estaba en movimiento hacia
     * el barril puede disparar
     */
    private void tomarRum(Barrel bar, ArrayList<Ship> enemyShips,
            ArrayList<Barrel> barrels, ArrayList<Mine> mines) {
        position.moveToTarget(this, bar.getPosition(), enemyShips, barrels, mines);
    }

    /**
     * toma el Rum del barril que esté mas cerca. Si estaba en movimiento hacia
     * el barril puede disparar
     */
    private void alejarseEnemigo(Barrel bar, Ship enemyTarget, ArrayList<Ship> enemyShips,
            ArrayList<Barrel> barrels, ArrayList<Mine> mines) {
        position.moveToTarget(this, bar.getPosition(), enemyShips, barrels, mines);
    }

    /**
     * Busca el mejor barril segun el Rum y la distancia. El mejor barril es el
     * que maximiza rum/distancia
     *
     * @param barrels
     * @return
     */
    private Barrel buscarMejorBarril(ArrayList<Barrel> barrels) {
        Barrel barrelTargetMax = null;
        float barrelHeuristicActual;
        float barrelHeuristicMax = 0;
        int barrelDistance;
        int barrelRum = 0;
        for (Barrel barrel : barrels) {
            barrelDistance = barrel.getPosition().hexDistance(position.hexOffsetNeighbor(orientation));
            barrelRum = barrel.getRum();
            barrelHeuristicActual = barrelRum / barrelDistance;
            if (barrelHeuristicMax < barrelHeuristicActual) {
                barrelTargetMax = barrel;
                barrelHeuristicMax = barrelHeuristicActual;
            }

        }
        System.err.println("Heuristic [Barrel Target] = " + barrelHeuristicMax);
        if (barrelTargetMax != null) {
            System.err.println("Position [Barrel Target] = " + barrelTargetMax.getPosition().toString());
        }
        return barrelTargetMax;
    }

    /**
     * Busca el mejor barril segun el Rum y la distancia del enemigo. El mejor
     * barril es el que maximiza rum/distanciaEnemigo
     *
     * @param barrels
     * @return
     */
    private Barrel buscarMejorBarril(ArrayList<Barrel> barrels, Ship enemyTarget) {
        Barrel barrelTargetMax = null;
        float barrelHeuristicActual;
        float barrelHeuristicMax = 0;
        Hex enemyPosition = enemyTarget.getPosition();
        int enemyDistance;
        int barrelRum = 0;
        for (Barrel barrel : barrels) {
            enemyDistance = barrel.getPosition().hexDistance(enemyPosition.hexOffsetNeighbor(enemyTarget.getOrientation()));
            barrelRum = barrel.getRum();
            barrelHeuristicActual = barrelRum / enemyDistance;
            if (barrelHeuristicMax < barrelHeuristicActual) {
                barrelTargetMax = barrel;
                barrelHeuristicMax = barrelHeuristicActual;
            }
        }
        System.err.println("Heuristic [Barrel Target] = " + barrelHeuristicMax);
        if (barrelTargetMax != null) {
            System.err.println("Position [Barrel Target] = " + barrelTargetMax.getPosition().toString());
        }
        return barrelTargetMax;
    }

    /**
     * Busca el enemigo mas cercano. Si hay 2 enemigos en el radio de disparo,
     * devuelve el que tenga menos vida.
     *
     * @return
     */
    private Ship searchShortEnemy(ArrayList<Ship> enemyShips) {
        Ship enemyTarget = null;
        int distanceTarget = Integer.MAX_VALUE;
        int distanceToEnemy;
        for (Ship enemyShip : enemyShips) {
            distanceToEnemy = position.hexDistance(enemyShip.getPosition());
            if (enemyTarget == null) {
                distanceTarget = distanceToEnemy;
                enemyTarget = enemyShip;
            } else if (distanceToEnemy > 7 && distanceTarget > distanceToEnemy) {
                distanceTarget = distanceToEnemy;
                enemyTarget = enemyShip;
            } else if (distanceToEnemy <= 7 && distanceTarget <= 7) {
                if (enemyShip.getRum() < enemyTarget.getRum()) {
                    distanceTarget = distanceToEnemy;
                    enemyTarget = enemyShip;
                }
            }
        }
        if (enemyTarget != null) {
            System.err.println("Position [EnemyShort]: " + enemyTarget.getPosition().toString());
        }
        return enemyTarget;
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
    private int[][][] hexDirections
            = {{{1, 0}, {0, -1}, {-1, -1}, {-1, 0}, {-1, 1}, {0, 1}},
            {{1, 0}, {1, -1}, {0, -1}, {-1, 0}, {0, 1}, {1, 1}}};

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

    public boolean equals(Hex obj) {
        return (this.x == obj.getX() && this.y == obj.getY());
    }

    @Override
    public String toString() {
        return "(" + x + "," + y + ")";
    }

    public Hex hexOffsetNeighbor(int direction) {
        //devuelve la coordenada de el hexagono vecino en la direccion indicada
        if (hexOutOfMap()) {
            return this;
        }
        int even = this.getY() % 2;
        int[] dir = hexDirections[even][direction];
        return new Hex(this.getX() + dir[0], this.getY() + dir[1]);
    }

    public Hex cubeToHex(Cube cube) {
        //traduce de cubo a hexagono
        int cubeX = cube.getX();
        int cubeZ = cube.getZ();
        int hexX = cubeX + (cubeZ - (cubeZ % 2)) / 2;
        int hexY = cubeZ;
        Hex hex = new Hex(hexX, hexY);
        return hex;
    }

    public Cube hexToCube(Hex hex) {
        //traduce de hexagono a cubo
        int hexX = hex.getX();
        int hexY = hex.getY();
        int cubeX = hexX - (hexY - (hexY % 2)) / 2;
        int cubeZ = hexY;
        int cubeY = -cubeX - cubeZ;
        Cube cube = new Cube(cubeX, cubeY, cubeZ);
        return cube;
    }

    public int cubeDistance(Cube cubeA, Cube cubeB) {
        //distancia entre dos cubos
        return (Math.abs(cubeA.getX() - cubeB.getX()) + Math.abs(cubeA.getY()
                - cubeB.getY()) + Math.abs(cubeA.getZ() - cubeB.getZ())) / 2;
    }

    public int hexDistance(Hex hexB) {
        //distancia entre dos hexagonos
        Cube cubeA = hexToCube(this);
        Cube cubeB = hexToCube(hexB);
        return cubeDistance(cubeA, cubeB);
    }

    public boolean hexCollideMine(ArrayList<Mine> mines) {
        boolean collide = false;
        int i = 0;
        while (i < mines.size() && collide == false) {
            if (this.equals(mines.get(i).getPosition())) {
                collide = true;
            }
            i++;
        }
        return collide;
    }

    public boolean hexCollideEnemyShip(ArrayList<Ship> enemyShips) {
        boolean collide = false;
        int i = 0;
        while (i < enemyShips.size() && collide == false) {
            if (this.equals(enemyShips.get(i).getPosition())) {
                collide = true;
            }
            i++;
        }
        return collide;
    }

    public boolean hexCollideAllyShip(ArrayList<Ship> allyShips) {
        boolean collide = false;
        int i = 0;
        while (i < allyShips.size() && collide == false) {
            if (this.equals(allyShips.get(i).getPosition())) {
                collide = true;
            }
            i++;
        }
        return collide;
    }

    public boolean hexOutOfMap() {
        boolean outOfMap = false;
        if (this.getX() < 0 || this.getY() < 0 || this.getX() > 22 || this.getY() > 20) {
            outOfMap = true;
        }
        return outOfMap;
    }

    public boolean hexBorderOfMap() {
        boolean outOfMap = false;
        if (this.getX() < 1 || this.getY() < 1 || this.getX() > 21 || this.getY() > 19) {
            outOfMap = true;
        }
        return outOfMap;
    }

    public void moveToTarget(Ship ship, Hex posTarget, ArrayList<Ship> enemysShip,
            ArrayList<Barrel> barrels, ArrayList<Mine> mines) {
        Hex posShip = ship.getPosition();
        Hex hexForward = posShip.hexOffsetNeighbor(ship.getOrientation());

        Hex hexLeftTop = posShip.hexOffsetNeighbor(ship.getLeftDirection());
        Hex hexLeftMid = null;
        Hex hexLeftBot = null;
        if (!hexLeftTop.hexBorderOfMap()) {
            hexLeftMid = hexLeftTop.hexOffsetNeighbor(getOppositeDirection(ship.getLeftDirection()));
            hexLeftBot = hexLeftMid.hexOffsetNeighbor(getOppositeDirection(ship.getLeftDirection()));
        }

        Hex hexRightTop = posShip.hexOffsetNeighbor(ship.getRightDirection());
        Hex hexRightMid = null;
        Hex hexRightBot = null;
        if (!hexRightTop.hexBorderOfMap()) {
            hexRightMid = hexRightTop.hexOffsetNeighbor(getOppositeDirection(ship.getRightDirection()));
            hexRightBot = hexRightMid.hexOffsetNeighbor(getOppositeDirection(ship.getRightDirection()));
        }

        Hex hexStraightTop = hexForward.hexOffsetNeighbor(ship.getOrientation());
        //Hex hexStraightMid = hexStraightTop.hexOffsetNeighbor(getOppositeDirection(ship.getOrientation()));
        //Hex hexStraightBot = hexStraightMid.hexOffsetNeighbor(getOppositeDirection(ship.getOrientation()));

        Hex hexTurnLeftTop = hexForward.hexOffsetNeighbor(ship.getLeftDirection());
        Hex hexTurnLeftMid = null;
        Hex hexTurnLeftBot = null;
        if (!hexTurnLeftTop.hexBorderOfMap()) {
            hexTurnLeftMid = hexTurnLeftTop.hexOffsetNeighbor(getOppositeDirection(ship.getLeftDirection()));
            hexTurnLeftBot = hexTurnLeftMid.hexOffsetNeighbor(getOppositeDirection(ship.getLeftDirection()));
        }

        Hex hexTurnRightTop = hexForward.hexOffsetNeighbor(ship.getRightDirection());
        Hex hexTurnRightMid = null;
        Hex hexTurnRightBot = null;
        if (!hexTurnRightTop.hexBorderOfMap()) {
            hexTurnRightMid = hexTurnRightTop.hexOffsetNeighbor(getOppositeDirection(ship.getRightDirection()));
            hexTurnRightBot = hexTurnRightMid.hexOffsetNeighbor(getOppositeDirection(ship.getRightDirection()));
        }

        int distanceStraight = hexDistance(hexStraightTop);
        int distanceTurnLeft = hexDistance(hexTurnLeftTop);
        int distanceTurnRight = hexDistance(hexTurnRightTop);

        if (ship.getSpeed() < 1) {
            if (!hexStraightTop.hexOutOfMap()
                    && !hexStraightTop.hexCollideMine(mines) //&& !hexCollideMine(hexStraightMid) && !hexCollideMine(hexStraightBot)
                    && !hexStraightTop.hexCollideEnemyShip(enemysShip)) {
                System.out.println("FASTER");
                //System.err.println("[Hex Straight] = (" + hexStraightTop.getX() + "," + hexStraightTop.getY() + ")");
            } else if (!hexLeftTop.hexOutOfMap() && hexLeftMid != null && hexLeftBot != null
                    && !hexLeftTop.hexCollideMine(mines) && !hexLeftMid.hexCollideMine(mines) && !hexLeftBot.hexCollideMine(mines)
                    && !hexLeftTop.hexCollideEnemyShip(enemysShip) && distanceTurnLeft <= distanceTurnRight) {
                System.out.println("PORT");
            } else if (!hexRightTop.hexOutOfMap() && hexRightMid != null && hexRightBot != null
                    && !hexRightTop.hexCollideMine(mines) && !hexRightMid.hexCollideMine(mines) && !hexRightBot.hexCollideMine(mines)
                    && !hexRightTop.hexCollideEnemyShip(enemysShip) && distanceTurnRight <= distanceTurnLeft) {
                System.out.println("STARBOARD");
            } else if (!hexLeftTop.hexOutOfMap() && hexLeftMid != null && hexLeftBot != null
                    && !hexLeftTop.hexCollideMine(mines) && !hexLeftMid.hexCollideMine(mines) && !hexLeftBot.hexCollideMine(mines)
                    && !hexLeftTop.hexCollideEnemyShip(enemysShip)) {
                System.out.println("PORT");
            } else {
                System.out.println("STARBOARD");
            }
        } else {
            if (!hexStraightTop.hexOutOfMap()
                    && !hexStraightTop.hexCollideMine(mines) //&& !hexCollideMine(hexStraightMid) && !hexCollideMine(hexStraightBot)
                    && !hexStraightTop.hexCollideEnemyShip(enemysShip) && distanceStraight < distanceTurnLeft && distanceStraight < distanceTurnRight) {
                //System.out.println("MOVE " + hexStraight.getX() + " " + hexStraight.getY());
                System.out.println("WAIT");
            } else if (!hexLeftTop.hexOutOfMap() && hexTurnLeftMid != null && hexTurnLeftBot != null
                    && !hexTurnLeftTop.hexCollideMine(mines) && !hexTurnLeftMid.hexCollideMine(mines) && !hexTurnLeftBot.hexCollideMine(mines)
                    && !hexTurnLeftTop.hexCollideEnemyShip(enemysShip) && distanceTurnLeft <= distanceStraight && distanceTurnLeft <= distanceTurnRight) {
                //System.out.println("MOVE " + hexTurnLeft.getX() + " " + hexTurnLeft.getY());
                System.out.println("PORT");
            } else if (!hexRightTop.hexOutOfMap() && hexTurnRightMid != null && hexTurnRightBot != null
                    && !hexTurnRightTop.hexCollideMine(mines) && !hexTurnRightMid.hexCollideMine(mines) && !hexTurnRightBot.hexCollideMine(mines)
                    && !hexTurnRightTop.hexCollideEnemyShip(enemysShip) && distanceTurnRight <= distanceStraight && distanceTurnRight <= distanceTurnLeft) {
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

    private int getOppositeDirection(int direction) {
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
