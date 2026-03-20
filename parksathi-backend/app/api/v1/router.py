"""This file mainly handles all the routes of the v1 api"""
from fastapi import APIRouter, Depends
from .endpoints.health import health_handler
from .endpoints.user import process_reservation
from auth.firebase import verify_firebase_token

router = APIRouter()

@router.get("/health")
async def health():
    return await health_handler()

@router.get("/park")
async def reserve_park(user: dict = Depends(verify_firebase_token)):
    return await process_reservation()