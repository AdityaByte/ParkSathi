from pydantic import BaseModel
from datetime import datetime
from app.model.booking import BookingStatus
from uuid import UUID

class BookingResponse(BaseModel):
    uid: str
    booking_id: UUID
    parking_id: UUID
    booking_status: BookingStatus
    parking_name: str
    acquired_at: datetime | None = None