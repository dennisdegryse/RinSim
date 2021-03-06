/**
 * 
 */
package rinde.sim.pdptw.central;

import static com.google.common.base.Preconditions.checkArgument;

import javax.annotation.Nullable;
import javax.measure.quantity.Duration;
import javax.measure.quantity.Length;
import javax.measure.quantity.Velocity;
import javax.measure.unit.Unit;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import rinde.sim.core.graph.Point;
import rinde.sim.pdptw.common.ParcelDTO;
import rinde.sim.pdptw.common.VehicleDTO;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

/**
 * An immutable state object representing the state of an entire
 * {@link rinde.sim.core.Simulator} configured using
 * {@link rinde.sim.pdptw.common.DynamicPDPTWProblem}.
 * @author Rinde van Lon <rinde.vanlon@cs.kuleuven.be>
 */
public class GlobalStateObject {
  // TODO add generic way for storing travel distances based on shortest path
  // in road model

  /**
   * All known parcels which require both a pickup and a delivery. They are not
   * in the inventory of a vehicle.
   */
  public final ImmutableSet<ParcelDTO> availableParcels;

  /**
   * All vehicles.
   */
  public final ImmutableList<VehicleStateObject> vehicles;

  /**
   * The current time.
   */
  public final long time;

  /**
   * The unit of time.
   */
  public final Unit<Duration> timeUnit;

  /**
   * The unit of (vehicle) speed.
   */
  public final Unit<Velocity> speedUnit;

  /**
   * The unit of distances.
   */
  public final Unit<Length> distUnit;

  GlobalStateObject(ImmutableSet<ParcelDTO> availableParcels,
      ImmutableList<VehicleStateObject> vehicles, long time,
      Unit<Duration> timeUnit, Unit<Velocity> speedUnit, Unit<Length> distUnit) {
    this.availableParcels = availableParcels;
    this.vehicles = vehicles;
    this.time = time;
    this.timeUnit = timeUnit;
    this.speedUnit = speedUnit;
    this.distUnit = distUnit;
  }

  /**
   * Constructs a new {@link GlobalStateObject} with only the selected vehicle.
   * The current instance remains unchanged.
   * @param index The index of the vehicle to select.
   * @return A new object containing only the selected vehicle, all other values
   *         are copied from this instance.
   */
  public GlobalStateObject withSingleVehicle(int index) {
    checkArgument(index >= 0 && index < vehicles.size(),
        "Invalid vehicle index (%s) must be >= 0 and < %s.", index,
        vehicles.size());
    return new GlobalStateObject(availableParcels, ImmutableList.of(vehicles
        .get(index)), time, timeUnit, speedUnit, distUnit);
  }

  @Override
  public String toString() {
    return new ReflectionToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
        .toString();
  }

  /**
   * Immutable state object of a vehicle.
   * @author Rinde van Lon <rinde.vanlon@cs.kuleuven.be>
   */
  public static class VehicleStateObject extends VehicleDTO {
    private static final long serialVersionUID = -9021013328998791086L;

    /**
     * Location of the vehicle.
     */
    public final Point location;

    /**
     * The contents of the vehicle. This excludes parcels which are currently
     * being picked up and includes parcels which are currently being delivered.
     */
    public final ImmutableSet<ParcelDTO> contents;

    /**
     * The remaining time the vehicle needs for completion of its current
     * servicing operation.
     */
    public final long remainingServiceTime;

    /**
     * If present this field contains the route the vehicle is currently
     * following.
     */
    public final Optional<ImmutableList<ParcelDTO>> route;

    /**
     * This field is not <code>null</code> in two situations:
     * <ol>
     * <li>In case all of the following holds:
     * <ul>
     * <li>Vehicles are not allowed to divert from their previously started
     * routes.</li>
     * <li>The vehicle is moving to a parcel (either pickup or delivery
     * location).</li>
     * <li>The vehicle has not yet started servicing.</li>
     * </ul>
     * In this case it indicates the current destination of the vehicle. When a
     * vehicle has a destination it <b>must</b> first move to and service this
     * destination.</li>
     * <li>In case the vehicle is servicing a parcel. In this case the
     * {@link ParcelDTO} as specified by this field is the one being serviced.
     * In this case servicing <b>must</b> first complete before the vehicle can
     * do something else. When this {@link ParcelDTO} also occurs in
     * {@link #contents} this parcel is currently being delivered, otherwise it
     * is being picked up.</li>
     * </ol>
     */
    @Nullable
    public final ParcelDTO destination;

    VehicleStateObject(VehicleDTO dto, Point location,
        ImmutableSet<ParcelDTO> contents, long remainingServiceTime,
        @Nullable ParcelDTO destination,
        @Nullable ImmutableList<ParcelDTO> route) {
      super(dto.startPosition, dto.speed, dto.capacity,
          dto.availabilityTimeWindow);
      this.location = location;
      this.contents = contents;
      this.remainingServiceTime = remainingServiceTime;
      this.destination = destination;
      this.route = Optional.fromNullable(route);
    }

    @Override
    public String toString() {
      return new ReflectionToStringBuilder(this).toString();
    }
  }
}
