import java.awt.Color;

import ihs.apcs.spacebattle.*;
import ihs.apcs.spacebattle.commands.*;

import java.util.ArrayList;

public class MiddleShip extends BasicSpaceship {
   private Point center;

   private long shipMass;
   private int thrustPower = 3500;
   private double maxSpeed;
   private boolean startup = true;
   private ArrayList<ShipCommand> shipQueue;

    public static void main(String[] args)
    {
        TextClient.run("10.56.98.121", new MiddleShip());
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

      if (shipStatus.getSpeed() > 1.0) {
         return new BrakeCommand(0.0);
      }

      double distanceToCenter = shipStatus.getPosition().getDistanceTo(center);
      if (distanceToCenter < 69) {
         System.out.print(".");
         return new IdleCommand(0.1);
      }

      //hope this works
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
      double timeToHalfDistance = Math.sqrt( (double)(distanceToCenter-45)*shipMass/thrustPower );
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

      shipQueue.add(new BrakeCommand(0.0));

      //return first in queue
      return shipQueue.remove(0);
      
    }
}
