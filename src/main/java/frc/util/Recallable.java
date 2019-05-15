/*----------------------------------------------------------------------------*/
/* Copyright (c) 2018 FIRST. All Rights Reserved.                             */
/* Open Source Software - may be modified and shared by FRC teams. The code   */
/* must be accompanied by the FIRST BSD license file in the root directory of */
/* the project.                                                               */
/*----------------------------------------------------------------------------*/

package frc.util;

/**
 * Interface to implement for objects that can recall commands.
 */
public interface Recallable<T> {
    public enum CommandType {
        ROTATION,
        DISTANCE
    }

    public enum RecallMethod {
        REVERSE,
        REPEAT
    }

    public T recall();

    public CommandType getType();
}