from boa.interop.Neo.Runtime import GetTrigger, CheckWitness
from boa.interop.Neo.TriggerType import Application, Verification
from boa.interop.Neo.Storage import GetContext, Get, Put, Delete
from boa.builtins import concat
from boa.interop.Neo.Runtime import Notify

TOKEN_NAME = 'Example'
TOKEN_SYMBOL = 'EXP'
TOKEN_DECIMALS = 8
TOKEN_TOTAL_SUPPLY = 10000000 * 100000000

def Main(operation, args):
    if operation == 'name':
        return name()

    elif operation == 'decimals':
        return decimals()

    elif operation == 'symbol':
        return symbol()

    elif operation == 'totalSupply':
        return totalSupply()

    elif operation == 'balanceOf':
        assert len(args) == 1, 'incorrect arguments length'
        account = args[0]
        return balanceOf(account)

    elif operation == 'transfer':
        assert len(args) == 3, 'incorrect arguments length'
        return transfer(args[0], args[1], args[2])

    AssertionError('unknown operation')

def name():
    return TOKEN_NAME

def symbol():
    return TOKEN_SYMBOL

def totalSupply():
    return TOKEN_TOTAL_SUPPLY

def decimals():
    return TOKEN_DECIMALS

def balanceOf(account):
    ctx = GetContext()

    assert len(account) == 20, "invalid address"
    return Get(ctx, account)

def transfer(t_from, t_to, amount):
    ctx = GetContext()

    assert len(t_from) == 20, "Invalid from address"
    assert len(t_to) == 20, "Invalid to address"

    if t_from == t_to:
        Notify("Transferring to self. Nothing changes.")
        return True

    from_val = Get(ctx, t_from)
    if from_val == amount:
        Delete(ctx, t_from)
    else:
        difference = from_val - amount
        Put(ctx, t_from, difference)

    to_value = Get(ctx, t_to)
    to_total = to_value + amount
    Put(ctx, t_to, to_total)

    return True

def AssertionError(msg):
    Notify("Error occurred: " + msg)
    raise Exception(msg)
