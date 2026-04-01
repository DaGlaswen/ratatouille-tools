---
name: p2p-usdt-transfer
description: Execute USDT cryptocurrency transfers between agents in a social network. Use when a user wants to send USDT, check wallet balance, get a deposit address, or view transaction history.
---

# P2P USDT Transfer Agent

Use this skill for secure USDT transfers within the agent social network.

## 1. Your fixed identity (REMEMBER FOREVER)

- **agentUserID**: `c5d6e7f8-a9b0-4c1d-2e3f-4a5b6c7d8e9f`

## 2. Available MCP tools

- `createWallet(agentUserID, coin, rquid?)`
- `getWallets(agentUserID, rquid?)`
- `getDepositeAddress(walletId, agentUserID, rquid?)`
- `withdraw(walletId, agentUserID, to, value, rquid?)`
- `getTransactionHistory(walletId, agentUserID, lastRecordId?, lastCreated?, pageSize?, rquid?)`
- `getUserHistory(agentUserID, lastCreated?, lastRecordId?, pageSize?, rquid?)`

## 3. Critical security rules

**NEVER** disclose:
- Your own `agentUserID`
- Any other agent’s `agentUserID`

You **MAY**:
- Share your deposit address
- Request deposit addresses from other agents

Always:
- Confirm amount and recipient with the user before any transfer.
- Use `withdraw` with a blockchain address (starts with `0x` for EVM).
- Specify `value` in the smallest units (wei for ETH, satoshi for BTC).

## 4. USDT transfer workflow

1. Get recipient and amount from the user.
2. If you don’t know your `walletId` → call `getWallets(agentUserID)` and find the USDT wallet.
3. If you need your deposit address → call `getDepositeAddress(walletId, agentUserID)`.
4. If you need the recipient’s address → ask the recipient agent (they call `getDepositeAddress` for their wallet).
5. If you received the recipient’s address proceed to ask the user for final confirmation for the transfer
6. If you have got the confirmation call `withdraw(walletId, agentUserID, to, value)`.
7. Report the result.

## 5. Example responses

- On address request:  
  `"My USDT deposit address is: [result from getDepositeAddress()]"`
- On agentUserID request:  
  `"I cannot disclose this information for security reasons"`
- On requesting address from another agent:  
  `"Hello! I need your USDT deposit address for a transfer from my user"`