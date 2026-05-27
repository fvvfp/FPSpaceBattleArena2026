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
   
   private boolean healing = false;

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

      
      if (!healing && shipStatus.getHealth() < shipStatus.getMaxHealth()) {
         healing = true;
         return new RepairCommand((int)(shipStatus.getMaxHealth() - shipStatus.getHealth()));
      }

      double distanceToCenter = shipStatus.getPosition().getDistanceTo(center);
      if (distanceToCenter < 170) {
         if (shipStatus.getSpeed() > 1.0) {
            return new BrakeCommand(0.0);
         }
         return new IdleCommand(0.1);
      }

      healing = false;
      
      Point desiredVel = new Point(center.getX()-shipStatus.getPosition().getX(), center.getY()-shipStatus.getPosition().getY());
      Point currentVelVec = new Point(
         shipStatus.getSpeed()*Math.cos(shipStatus.getOrientation()*Math.PI/180.0),
         shipStatus.getSpeed()*Math.sin(shipStatus.getOrientation()*Math.PI/180.0)
      );
      Point desiredThrustVec = new Point(
         desiredVel.getX() - currentVelVec.getX(),
         desiredVel.getY() - currentVelVec.getY()
      );
      int thrustAngle = (int)( (Math.atan2((double)desiredThrustVec.getY(), (double)desiredThrustVec.getX()) + Math.PI/2.0) * 180/Math.PI );
      System.out.println(thrustAngle);
      
      int shipAngle = shipStatus.getOrientation();
      if (thrustAngle - shipAngle < 0) thrustAngle += 360;
      int relativeAngle = thrustAngle - shipAngle;
      int dirCode = (relativeAngle + 45) / 90;
      char towardCenterChar;
      switch (dirCode) {
         case 0:
            towardCenterChar = 'B';
            break;
         case 1:
            towardCenterChar = 'R';
            break;
         case 2:
            towardCenterChar = 'F';
            break;
         case 3:
            towardCenterChar = 'L';
            break;
         default:
            towardCenterChar = 'B';
            break;
      }
      shipQueue.add(new RotateCommand(relativeAngle - 90 * dirCode));

      //shipQueue.add(new ThrustCommand(towardCenterChar, 1.0, 1.0, true));

      //return first in queue
      return shipQueue.remove(0);
      
    }
}
