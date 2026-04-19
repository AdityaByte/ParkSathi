"""This file mainly serves the booking routes."""
from http import HTTPStatus
from uuid import UUID, uuid4
import logging
import ast
from datetime import datetime
from beanie.operators import Set, Inc, In

from fastapi import HTTPException

from app.model.booking import Booking, BookingStatus
from app.model.parking import ParkingDetails
from app.model.user import User
from app.schema.booking import BookingResponse, OwnerBookingResponse

async def acquire_booking(booking_id: str, status: BookingStatus):
    booking_id = UUID(booking_id)
    booking = await Booking.find_one(Booking.booking_id == booking_id)
    
    if booking.booking_status == BookingStatus.ACQUIRED:
        return {"message": "This booking is already acquired."}
    
    booking.booking_status = status
    booking.acquired_at = datetime.now()
    
    result = await ParkingDetails.find_one(
        ParkingDetails.parking_id == booking.parking_id
    ).update(Inc({
        ParkingDetails.acquired_slots: 1,
        ParkingDetails.booked_slots: -1
    }))
    
    if result is None:
        raise HTTPException(status_code=HTTPStatus.INTERNAL_SERVER_ERROR, detail="Failed to save the updated parking details document.")
        
    await booking.save()
    return {"message": "Booking acquired successfully."}

async def create_booking(uid: str, parking_id: str) -> BookingResponse:
    # firstly we need to clean the parking_id.
    # Since the parking_id is the string representation of bytes we need to firstly convert it to bytes then we need to pass those bytes to the UUID function.
    parking_id_bytes = ast.literal_eval(parking_id)
    parking_id = UUID(bytes=parking_id_bytes)
    # Here we need to fetch the parking_details from the parking id and thus...
    parking_details = await ParkingDetails.find_one(ParkingDetails.parking_id == parking_id)
    if parking_details is None:
        logging.error(f"No parking details were found of the parking id {parking_id}")
        raise HTTPException(status_code=HTTPStatus.BAD_REQUEST, detail="No parking details were found of the parking id.")
    
    # Now if the details were found we need to make a booking id associated with the user id.
    # We don't need to check to the uid as of the authentication part is already done.
    # Each time a new booking is generated so we cannot upsert it.
    
    new_booking = await Booking(
        uid = uid,
        booking_id = uuid4(),
        booking_status=BookingStatus.BOOKED,
        created_at=datetime.now(),
        parking_id=parking_details.parking_id,
        parking_details=parking_details # linking is automatically handled by beanie.
    ).insert()
    
    # Now we need to update the parking details booking status
    # We don't need to check for the booking slots fullness as of we filtered and showed only those parking locations
    # whose have some empty slots so we just need to update here.
    parking_details.booked_slots += 1
    
    await parking_details.save() # crucial: by this we are reflecting the changes.
    
    # Now we just need to save the document.
    return BookingResponse(
        uid=new_booking.uid,
        booking_id=new_booking.booking_id,
        parking_id=new_booking.parking_id,
        booking_status=new_booking.booking_status,
        parking_name=parking_details.parking_name,
        acquired_at=new_booking.acquired_at,
    )
    
async def get_bookings(uid: str) -> list[BookingResponse]:
    bookings = await Booking.find(Booking.uid == uid).to_list()
    
    if not bookings:
        return []
        
    result: list = []
    
    for booking in bookings:
        parking = await booking.parking_details.fetch()
        
        result.append(
            BookingResponse(
                uid=booking.uid,
                booking_id=booking.booking_id,
                parking_id=booking.parking_id,
                booking_status=booking.booking_status,
                acquired_at=booking.acquired_at,
                parking_name=parking.parking_name or "" # Result out to be null then empty string is to returned.
            )
        )
    
    return result
    
async def get_bookings_by_parking_id(uid: str) -> list[OwnerBookingResponse]:
    parking_detail = await ParkingDetails.find_one(ParkingDetails.uid == uid)
    if not parking_detail:
        return []
        
    parking_id = parking_detail.parking_id
    bookings = await Booking.find(Booking.parking_id == parking_id).to_list()
    
    if not bookings:
        return []
        
    # Here we just have done a optimization rather of making many calls to the database we can find the users in one call
    # and then created a lookup dictionary whose key is uid and the value is the username.
    # As of in the previous code if the user is missing then the code will crash out abruptly but this one is optimized.
    user_uids = list(set(b.uid for b in bookings))
    users = await User.find(In(User.uid, user_uids)).to_list()
    user_map = {u.uid: u.name for u in users}

    result = []
    for booking in bookings:
        user_name = user_map.get(booking.uid, "Unknown User")
        
        result.append(OwnerBookingResponse(
            user_name=user_name,
            booking_id=booking.booking_id,
            booking_status=booking.booking_status,
            created_at=booking.created_at,
            acquired_at=booking.acquired_at,
            completed_at=booking.completed_at
        ))
        
    return result
    
async def cancel_booking(booking_id: UUID, uid: str):
    result = await Booking.find_one(
        Booking.booking_id == booking_id,
        Booking.uid == uid
    ).update(
        Set({Booking.booking_status: BookingStatus.CANCELLED})
    )

    if result is None:
        raise HTTPException(status_code=404, detail="Booking not found")
        
    parking_result = await ParkingDetails.find_one(ParkingDetails.parking_id == result.parking_id).update(
        Set({ParkingDetails.booked_slots: ParkingDetails.booked_slots - 1})
    )
    
    if parking_result is None:
        raise HTTPException(status_code=HTTPStatus.INTERNAL_SERVER_ERROR, detail="Failed to save the parking result while cancelling the booking.")