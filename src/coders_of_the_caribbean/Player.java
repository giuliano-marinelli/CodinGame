
import java.util.*;
import java.io.*;
import java.math.*;

class Player {

    private static ArrayList<Ship> allyShips = new ArrayList<>();
    private static ArrayList<Ship> enemyShips = new ArrayList<>();
    private static ArrayList<Barrel> barrels = new ArrayList<>();
    private static ArrayList<Mine> mines = new ArrayList<>();
    private static ArrayList<Cannonball> cannonballs = new ArrayList<>();
    private static ArrayList<Hex> obstacles = new ArrayList<>();
    private static ArrayList<String> actions = new ArrayList<>(
            Arrays.asList(new String[]{"WAIT", "SLOWER", "FASTER", "STARBOARD",
        "PORT", "MINE", "FIRE"}));

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
                    if (arg2 == 1) {
                        obstacles.add(new Hex(x, y));
                    }
                }
            }

            for (int i = 0; i < allyShips.size(); i++) {
                allyShips.get(i).buscarMovimiento(allyShips, enemyShips, barrels, mines, actions);
            }
        }
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

    public void buscarMovimiento(ArrayList<Ship> allyShips, ArrayList<Ship> enemyShips,
            ArrayList<Barrel> barrels, ArrayList<Mine> mines, ArrayList<String> actions) {
        /*INSERTE AQUI SU ESTRATEGIA
        la mejor estrategia es aquella en que la función de valuación devuelve 
        un valor mas alto.
        No se analizan todas las acciones posibles para un barco, la estrategia limita
        la cantidad de acciones "buenas" segun la cantidad de vida (rum) de cada barco*/
        if (rum > 80) {
            System.err.println("[primer estrategia]");
            /*se acerca al mejor rum pero no lo agarra.*/
            goBarrel(allyShips, enemyShips, barrels, mines);
        } else if (rum > 60) {
            System.err.println("[segunda estrategia]");
            /*toma el rum.*/
            tomarRum(allyShips, enemyShips, barrels, mines);
        } else {
            System.err.println("[tercera estrategia]");
            /*alejarse del enemigo en busca del Rum que maximice el rum/distanciaEnemigo*/
            alejarseEnemigoBuscarRum(allyShips, enemyShips, barrels, mines);
        }
    }

    /**
     * Se acerca al barril pero no lo toma. Si estaba en movimiento hacia el
     * barril puede disparar
     */
    private void goBarrel(ArrayList<Ship> allyShips, ArrayList<Ship> enemyShips,
            ArrayList<Barrel> barrels, ArrayList<Mine> mines) {
        Barrel bar = buscarMejorBarril(barrels);
        if (bar != null) {
            Entity newEntity = new Entity(bar.getId(), new Hex(bar.getSidePosition(orientation).getX(),
                    bar.getSidePosition(orientation).getY()));
            goTo(newEntity, allyShips, enemyShips, barrels, mines);
        } else {
            //si no quedan barriles
            finalStrategy(enemyShips);
        }
    }

    /**
     * toma el Rum del barril que esté mas cerca. Si no quedan barriles puede
     * disparar
     */
    private void tomarRum(ArrayList<Ship> allyShips, ArrayList<Ship> enemyShips,
            ArrayList<Barrel> barrels, ArrayList<Mine> mines) {
        Barrel bar = buscarMejorBarril(barrels);
        if (bar != null) {
            goTo(bar, allyShips, enemyShips, barrels, mines);
        } else {
            //si no quedan barriles.
            finalStrategy(enemyShips);
        }
    }

    /**
     * se aleja del enemigo en busca de un barril.
     */
    private void alejarseEnemigoBuscarRum(ArrayList<Ship> allyShips, ArrayList<Ship> enemyShips,
            ArrayList<Barrel> barrels, ArrayList<Mine> mines) {
        Ship enemyTarget = searchShortEnemy(enemyShips);
        Barrel bar = buscarMejorBarril(barrels, enemyTarget);
        if (bar != null) {
            goTo(bar, allyShips, enemyShips, barrels, mines);
        } else {
            /*si no quedan barriles y se tiene mas vida que el enemigo alejarse. 
            Si se tiene menos vida que el enemigo tratar de matarlo primero.*/
            finalStrategy(enemyShips);
        }
    }

    /**
     * Si no quedan barriles se aleja del enemigo si tiene mas vida o se acerca
     * a matarlo si tiene menos vida.
     *
     * @param enemyShips
     */
    private void finalStrategy(ArrayList<Ship> enemyShips) {
        System.err.println("[Final Strategy]");
        Ship enemyTarget = searchShortEnemy(enemyShips);
        if (rum > enemyTarget.getRum()) {
            alejarseEnemigo(enemyTarget);
        } else {
            /*buscar enemigo*/
            buscarEnemigo(enemyTarget);
        }
    }

    private void alejarseEnemigo(Ship enemyTarget) {
        Hex newPosition;
        int oppEnemy = Direction.invert(enemyTarget.getOrientation());
        newPosition = position.getNeighbor(oppEnemy);
        if (!newPosition.isOutOfMap()) {
            System.out.println("MOVE " + newPosition.getX() + " " + newPosition.getY());
        } else {
            newPosition = position.getNeighbor(getPortDirection());
            if (!newPosition.isOutOfMap()) {
                System.out.println("MOVE " + newPosition.getX() + " " + newPosition.getY());
            } else {
                newPosition = position.getNeighbor(getStarboardDirection());
                System.out.println("MOVE " + newPosition.getX() + " " + newPosition.getY());
            }
        }
    }

    private void buscarEnemigo(Ship enemyTarget) {
        Hex newPosition;
        newPosition = enemyTarget.getPosition();
        System.out.println("MOVE " + newPosition.getX() + " " + newPosition.getY());
    }

    private void goTo(Entity entity, ArrayList<Ship> allyShips, ArrayList<Ship> enemyShips,
            ArrayList<Barrel> barrels, ArrayList<Mine> mines) {
        if (speed == 0) {
            System.out.println("MOVE " + entity.getPosition().getX() + " " + entity.getPosition().getY());
        } else {
            //mientras esta yendo a algun lugar puede disparar
            disparar(allyShips, enemyShips, mines);
        }
    }

    /**
     * puede disparar a minas o ships. los ships enemigos tienen prioridad
     *
     * @param enemyTarget
     */
    public boolean disparar(ArrayList<Ship> allyShips, ArrayList<Ship> enemyShips, ArrayList<Mine> mines) {
        boolean disparo;
        disparo = dispararEnemigo(enemyShips);
        if (!disparo) {
            disparo = dispararMina(mines);
            if (!disparo) {
                if (speed == 0) {
                    System.out.println("WAIT");
                } else {
                    System.out.println("SLOWER");
                }
            }
        }
        return disparo;
    }

    private boolean dispararMina(ArrayList<Mine> mines) {
        boolean disparo = false;
        /*disparar a minas cercanas*/
        Mine mine = searchMineOnDirection(mines);
        if (mine != null) {
            disparo = true;
            System.out.println("FIRE " + mine.getPosition().getX() + " " + mine.getPosition().getY());
        }
        return disparo;
    }

    private boolean dispararEnemigo(ArrayList<Ship> enemyShips) {
        boolean disparo = false;
        Ship enemyTarget = searchShortEnemy(enemyShips);
        if (enemyTarget != null) {
            int distanceToEnemy = position.distanceTo(enemyTarget.getPosition());
            if (distanceToEnemy <= 10) {
                int attackDirection = enemyTarget.getOrientation();
                Hex futureEnemyHex = enemyTarget.getPosition();
                int j = 0;
                while (j < (1 + distanceToEnemy / 3) * enemyTarget.getSpeed() && !futureEnemyHex.isOutOfMap()) {
                    futureEnemyHex = futureEnemyHex.getNeighbor(attackDirection);
                    j++;
                }
                System.err.println("[Disparar enemigo]");
                System.err.println("Distance [EnemyShip] = " + distanceToEnemy);
                disparo = true;
                System.out.println("FIRE " + futureEnemyHex.getX() + " " + futureEnemyHex.getY());
            }
        }
        return disparo;
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
            barrelDistance = barrel.distanceTo(position.getNeighbor(orientation));
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
            enemyDistance = barrel.distanceTo(enemyPosition.getNeighbor(enemyTarget.getOrientation()));
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
            distanceToEnemy = position.distanceTo(enemyShip.getPosition());
            if (enemyTarget == null) {
                distanceTarget = distanceToEnemy;
                enemyTarget = enemyShip;
            } else if (distanceToEnemy > 10 && distanceTarget > distanceToEnemy) {
                distanceTarget = distanceToEnemy;
                enemyTarget = enemyShip;
            } else if (distanceToEnemy <= 10 && distanceTarget <= 10) {
                if (enemyShip.getRum() < enemyTarget.getRum()) {
                    distanceTarget = distanceToEnemy;
                    enemyTarget = enemyShip;
                }
            }
        }
        return enemyTarget;
    }

    /**
     * Busca una mina en el camino del barco
     *
     * @return
     */
    private Mine searchMineOnDirection(ArrayList<Mine> mines) {
        Mine mineTarget = null;
        Hex camino;
        int i;
        for (Mine mine : mines) {
            i = 0;
            while (i < 5 && mineTarget != null) {
                camino = position.getNeighbor(orientation, i);
                if (!camino.isOutOfMap()) {
                    if (mine.getPosition().equals(camino)) {
                        mineTarget = mine;
                    }
                }
                i++;
            }
        }
        if (mineTarget != null) {
            System.err.println("Position [mine]: " + mineTarget.getPosition().toString());
        }
        return mineTarget;
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

    public int distanceTo(Hex pos) {
        return position.distanceTo(pos);
    }

    public Hex getSidePosition(int orientation) {
        return position.getNeighbor(orientation);
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

    public String toString() {
        return "(" + x + "," + y + ")";
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

    public boolean equals(Hex obj) {
        return (this.x == obj.getX() && this.y == obj.getY());
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
