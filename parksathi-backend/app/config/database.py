from motor.motor_asyncio import AsyncIOMotorClient
from beanie import init_beanie
from app.config.config import settings
import logging

from app.model.booking import Booking
from app.model.parking import ParkingDetails
from app.model.user import User


async def init_db():
    client = AsyncIOMotorClient(settings.mongo_uri)
    try:
        await init_beanie(database=client.parksathi, document_models=[User, ParkingDetails, Booking])
        await client.admin.command("ping")
        logging.info("Connected to the mongodb successfully")
    except Exception as e:
        print(f"Failed to connect to mongodb: {e}")
        raise e