from beanie import Document, Link
from pydantic import Field
from uuid import UUID, uuid4
from enum import Enum
from datetime import datetime

from app.model.parking import ParkingDetails

class BookingStatus(str, Enum):
    BOOKED = "booked" # When the user clicks on the book btn.
    ACQUIRED = "acquired" # When the user scans the QR and the vehicle is on spot.
    COMPLETED = "completed" # User left, payment done.
    CANCELLED = "cancelled" # User cancelled the booking by own.
    EXPIRED = "expired" # User never acquired the spot.

class Booking(Document):
    uid: str # This is the unique key which maps to the user booking.
    booking_id: UUID = Field(default_factory=uuid4)
    parking_details: Link[ParkingDetails]
    booking_status: BookingStatus
    created_at: datetime = Field(default_factory=datetime.utcnow)
    acquired_at: datetime | None = None
    completed_at: datetime | None = None
    
    class Settings:
        name = "bookings"