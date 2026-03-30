"""This file has a schema for the dashboard"""
from pydantic import BaseModel

class DashboardResponse(BaseModel):
    total_income: float
    total_slots: int
    booked_slots: int
    acquired_slots: int
    available_slots: int