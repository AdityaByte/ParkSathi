"""This file mainly contains the health controller"""

async def health_handler():
    return {"status": "OK"}