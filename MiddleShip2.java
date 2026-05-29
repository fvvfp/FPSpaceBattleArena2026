import java.awt.Color;

import ihs.apcs.spacebattle.*;
import ihs.apcs.spacebattle.commands.*;

import java.util.ArrayList;

public class MiddleShip2 extends BasicSpaceship {
   private Point center;

   private long shipMass;
   private int thrustPower = 3500;
   private double maxSpeed;
   private boolean startup = true;
   private ArrayList<ShipCommand> shipQueue;
   private boolean justDidRadar = false;

    public static void main(String[] args)
    {
        TextClient.run("10.56.98.121", new MiddleShip2());
    }

    @Override
    public RegistrationData registerShip(int numImages, int worldWidth, int worldHeight)
    {
      center = new Point(worldWidth / 2.0, worldHeight / 2.0);

      shipQueue = new ArrayList<ShipCommand>();
      
      return new RegistrationData("FP", new Color(205, 205, 255), 2);
    }

    @Override
    public ShipCommand getNextCommand(BasicEnvironment env)
    {
      
      if (!shipQueue.isEmpty()) {
         ShipCommand c = shipQueue.remove(0);
         return c;
      }

      ObjectStatus shipStatus = env.getShipStatus();
      
      if (startup) {
         shipMass = shipStatus.getMass();
         maxSpeed = shipStatus.getMaxSpeed();
         startup = false;
      }
      
      if (justDidRadar) {
         justDidRadar = false;
         return doTorpedo(shipStatus, env.getRadar());
      }

      if (shipStatus.getSpeed() > 2.0) {
         return new BrakeCommand(0.0);
      }
      
      if (shipStatus.getEnergy() > 50.0) {
         justDidRadar = true;
         return new RadarCommand(5);
      }

      double distanceToCenter = shipStatus.getPosition().getDistanceTo(center);
      if (distanceToCenter < 175) {
         if (shipStatus.getEnergy() >= 15) {
            return new RadarCommand(5);
         } else {
            return new IdleCommand(0.1);
         }
      }
      
      doMovement(shipStatus, distanceToCenter);

      //return first in queue
      return shipQueue.remove(0);
      
    }
    
    public ShipCommand doTorpedo(ObjectStatus shipStatus, ArrayList<ObjectStatus> radarResults) {
      ObjectStatus target = null;
      double score = 999999.09;
      for (ObjectStatus object : radarResults) {
         if (object.getType().equals("Ship") || object.getType().equals("Asteroid")) {
            double oScore = object.getSpeed();
            int angleOffset = Math.abs(Math.abs(object.getMovementDirection() - shipStatus.getPosition().getAngleTo(object.getPosition())) - 180);
            if (angleOffset <= 15) {
               oScore *= -1.0;
            } else if (object.getType().equals("Ship")) {
               oScore += object.getPosition().getDistanceTo(center) * 0.5;
               //target chud ship???
            } else {
               oScore += 150.0;
            }

            
            if (oScore < score) {
               score = oScore;
               target = object;
            }
         }
      }
      
      if (target == null) return new IdleCommand(0.1);

      int angleToTarget = shipStatus.getPosition().getAngleTo(target.getPosition());
      int shipOrientation = shipStatus.getOrientation();
      if (angleToTarget - shipOrientation < 0) angleToTarget += 360;
      int relativeAngle = angleToTarget - shipOrientation;
      int dirCode = (relativeAngle + 90) / 180;
      if ((dirCode == 1) {
         shipQueue.add(new FireTorpedoCommand('B'));
      } else {
         shipQueue.add(new FireTorpedoCommand('F'));
      }
      shipQueue.add(new IdleCommand(0.2));
      return new RotateCommand(relativeAngle - 180*dirCode);
    }
    
    public void doMovement(ObjectStatus shipStatus, double distanceToCenter) {
      int angleToCenter = shipStatus.getPosition().getAngleTo(center);
      int shipAngle = shipStatus.getOrientation();
      if (angleToCenter - shipAngle < 0) angleToCenter += 360;
      int relativeAngle = angleToCenter - shipAngle;
      int dirCode = (relativeAngle + 45) / 90;
      char towardCenterChar;
      char awayFromCenterChar;
      switch (dirCode) {
         case 0:
            towardCenterChar = 'B';
            awayFromCenterChar = 'F';
            break;
         case 1:
            towardCenterChar = 'R';
            awayFromCenterChar = 'L';
            break;
         case 2:
            towardCenterChar = 'F';
            awayFromCenterChar = 'B';
            break;
         case 3:
            towardCenterChar = 'L';
            awayFromCenterChar = 'R';
            break;
         default:
            towardCenterChar = 'B';
            awayFromCenterChar = 'F';
            break;
      }
      shipQueue.add(new RotateCommand(relativeAngle - 90 * dirCode));

      double timeToFullSpeed = maxSpeed * shipMass / thrustPower;
      double timeToHalfDistance = Math.sqrt( (double)(distanceToCenter-140)*shipMass/thrustPower );
      if (timeToFullSpeed > timeToHalfDistance) {
         shipQueue.add(new ThrustCommand(towardCenterChar, timeToHalfDistance, 1.0, true));
         shipQueue.add(new ThrustCommand(awayFromCenterChar, timeToHalfDistance, 1.0, true));
      } else {
         double distanceInTimeToFullSpeed = timeToFullSpeed*timeToFullSpeed*3500/2/shipMass;
         double distanceIdling = distanceToCenter - 2 * distanceInTimeToFullSpeed;
         double timeIdling = distanceIdling / maxSpeed;
         if (timeIdling >= 0.1) {
            shipQueue.add(new ThrustCommand(towardCenterChar, timeToFullSpeed, 1.0, true));
            shipQueue.add(new IdleCommand(timeIdling));
            shipQueue.add(new ThrustCommand(awayFromCenterChar, timeToFullSpeed, 1.0, true));
         } else {
            shipQueue.add(new ThrustCommand(towardCenterChar, timeToFullSpeed + timeIdling, 1.0, true));
            shipQueue.add(new ThrustCommand(awayFromCenterChar, timeToFullSpeed, 1.0, true));
         }
      }
    }
    
}
