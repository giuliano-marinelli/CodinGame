// game loop
while (true) {
    var inputs = readline().split(' ');
    var x = parseInt(inputs[0]);
    var y = parseInt(inputs[1]);
    var humanCount = parseInt(readline());
    var humans = new Array();
    var zombies = new Array();
    for (var i = 0; i < humanCount; i++) {
        var inputs = readline().split(' ');
        humans[i] = new Array();
        humans[i][0] = parseInt(inputs[0]); //id
        humans[i][1] = parseInt(inputs[1]); //x
        humans[i][2] = parseInt(inputs[2]); //y  
    }
    var zombieCount = parseInt(readline());
    for (var i = 0; i < zombieCount; i++) {
        var inputs = readline().split(' ');
        zombies[i] = new Array();
        zombies[i][0] = parseInt(inputs[0]);    //id
        zombies[i][1] = parseInt(inputs[1]);    //x
        zombies[i][2] = parseInt(inputs[2]);    //y
        zombies[i][3] = parseInt(inputs[3]);    //nextX
        zombies[i][4] = parseInt(inputs[4]);    //nextY
    }
    
    var bestHumanX = humans[0][1];
    var bestHumanY = humans[0][2];
    var bestZombieX = zombies[0][1];
    var bestZombieY = zombies[0][2];
    var bestNextZombieX = zombies[0][3];
    var bestNextZombieY = zombies[0][4];
    var bestDistance = Number.MAX_VALUE;
    for (var i = 0; i < humanCount; i++) {
        var distHuman = distanceTo(x,y,humans[i][1],humans[i][2]);
        var distZombie = Number.MAX_VALUE;
        var zombieX;
        var zombieY;
        for (var j = 0; j < zombieCount; j++) {
            var distZombieActual = distanceTo(humans[i][1],humans[i][2],zombies[j][1],zombies[j][2]);
            if (distZombieActual < distZombie) {
                distZombie = distZombieActual;
                zombieX = zombies[j][1];
                zombieY = zombies[j][2];
                nextZombieX = zombies[j][3];
                nextZombieY = zombies[j][4];
            }
        }
        var turnsZombie = turnsTo(distZombie,400,400);
        var turnsHuman = turnsTo(distHuman,1000,1600);
        printErr('turnsZ='+turnsZombie+" d"+distZombie);
        printErr('turnsH'+i+'='+turnsHuman+" d"+distHuman);
        if (turnsZombie > turnsHuman) {
            printErr("entro");
            var distZombieHuman = distZombie + distHuman;
            if (distZombieHuman<bestDistance) {
                bestHumanX = humans[i][1];
                bestHumanY = humans[i][2];
                bestZombieX = zombieX;
                bestZombieY = zombieY;
                bestNextZombieX = nextZombieX;
                bestNextZombieY = nextZombieY;
                bestDistance = distZombieHuman;
            }
        }
    }
    // Write an action using print()
    // To debug: printErr('Debug messages...');
    //var posZombie = posToKill(x,y,bestZombieX,bestZombieY,bestNextZombieX,bestNextZombieY);
    //print(posZombie[0]+" "+posZombie[1]);
    print(bestNextZombieX+" "+bestNextZombieY);
}

function distanceTo(x1, y1, x2, y2) {
    var distanceX = Math.abs(x1 - x2);
    var distanceY = Math.abs(y1 - y2);
    return Math.sqrt(distanceX*distanceX + distanceY*distanceY);
}

function turnsTo(distance,velocity,range) {
    return (distance - range) / velocity;
}

function posToKill(ashX,ashY,zombieX,zombieY,nextZombieX,nextZombieY) {
    var posZombie= new Array();
    var turn = 0;
    do {
        turn++;
        posZombie[0] = zombieX + turn * (nextZombieX - zombieX);
        posZombie[1] = zombieY + turn * (nextZombieY - zombieY);
    } while (distanceTo(ashX,ashY,posZombie[0],posZombie[1]) < turn * 1000 + 2000)
    return posZombie;
}