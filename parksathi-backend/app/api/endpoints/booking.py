"""This file mainly serves the booking routes."""
from http import HTTPStatus
from uuid import UUID, uuid4
import logging
import ast
from datetime import datetime
from beanie.operators import Set


from fastapi import HTTPException

from app.model.booking import Booking, BookingStatus
from app.model.parking import ParkingDetails
from app.schema.booking import BookingResponse

async def acquire_booking(uid: str, parking_id: str) -> BookingResponse:
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
                parking_name=parking.parking_name
            )
        )
    
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