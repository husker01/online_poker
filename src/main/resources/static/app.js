document.addEventListener('DOMContentLoaded', (event) => {
    document.getElementById('dealCards').addEventListener('click', dealCards);
    document.getElementById('restartGame').addEventListener('click', restartGame);

    const seatsContainer = document.getElementById('seats');
    for (let i = 1; i <= 10; i++) {
        const seatDiv = document.createElement('div');
        seatDiv.id = `seat-${i}`;
        seatDiv.className = 'seat';
        seatDiv.innerHTML = `
            <button onclick="handleSeatClick(${i})">Seat ${i}</button>
            <p id="player-name-seat-${i}"></p> <!-- Placeholder for player name -->
            <p id="buyin-amount-seat-${i}" class="buyin-amount"></p> <!-- Placeholder for buy-in amount -->
            <div id="card-container-seat-${i}" class="card-container"></div> <!-- Card container -->
            <div class="player-actions">
                <button onclick="handleBet(${i})">Bet</button>
                <button onclick="handleCheck(${i})">Check</button>
            </div>
        `;
        seatsContainer.appendChild(seatDiv);
    }
});


// Updated function to handle seat clicks
function handleSeatClick(seatNumber) {
    const playerNameDisplay = document.getElementById(`player-name-seat-${seatNumber}`);
    const cardContainer = document.getElementById(`card-container-seat-${seatNumber}`);

    // Check if the seat is currently occupied
    if (playerNameDisplay && playerNameDisplay.textContent) {
        // Seat is occupied, clear it
        playerNameDisplay.textContent = ''; // Clear player name
        if (cardContainer) {
            cardContainer.innerHTML = ''; // Clear cards
        }
        leaveSeat(seatNumber);
    } else {
        // Seat is not occupied, attempt to take it
        attemptToTakeSeat(seatNumber);
    }
}

function dealCards() {
    fetch('/poker/deal', {
        method: 'GET',
        headers: {
            'Content-Type': 'application/json',
        },
    })
        .then(response => response.json())
        .then(hands => {
            // Loop through all the seats
            for (let i = 1; i <= 10; i++) {
                const seatDiv = document.getElementById(`seat-${i}`);
                const playerNameDisplay = seatDiv.querySelector('#player-name-seat-' + i);
                const cardContainer = seatDiv.querySelector('#card-container-seat-' + i) || document.createElement('div');

                // Create the card container if it doesn't exist
                if (!seatDiv.contains(cardContainer)) {
                    cardContainer.id = `card-container-seat-${i}`;
                    cardContainer.classList.add('card-container');
                    seatDiv.appendChild(cardContainer);
                }

                cardContainer.innerHTML = ''; // Clear previous cards

                // Check if the seat is occupied (player name exists)
                if (playerNameDisplay && playerNameDisplay.textContent) {
                    const playerName = playerNameDisplay.textContent;
                    const cards = hands[playerName];

                    if (cards && cards.length > 0) {
                        cards.forEach(card => {
                            const imageFileName = `${card.rank.toLowerCase()}_of_${card.suit.toLowerCase()}.png`;
                            const imgElement = document.createElement('img');
                            imgElement.src = `images/cards/${imageFileName}`;
                            imgElement.alt = `${card.rank} of ${card.suit}`;
                            imgElement.classList.add('seat-card-img');

                            cardContainer.appendChild(imgElement);
                        });
                    }
                }
            }
        })
        .catch(error => {
            console.error('Error dealing cards:', error);
        });
}





function restartGame() {
    fetch('/poker/restart', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
            // Include CSRF token if needed
        }
    })
        .then(response => {
            if (!response.ok) {
                throw new Error('Failed to restart the game');
            }
            return response.text();
        })
        .then(message => {
            // Clear the card images, player names, and buy-in amounts
            for (let i = 1; i <= 10; i++) {
                const cardContainer = document.getElementById(`card-container-seat-${i}`);
                const playerNameDisplay = document.getElementById(`player-name-seat-${i}`);
                const buyInAmountDisplay = document.getElementById(`buyin-amount-seat-${i}`);

                if (cardContainer) {
                    cardContainer.innerHTML = '';
                }
                if (playerNameDisplay) {
                    playerNameDisplay.textContent = '';
                }
                if (buyInAmountDisplay) {
                    buyInAmountDisplay.textContent = '';
                }
            }

            // Display the message in the message container
            document.getElementById('message-container').innerText = message;
        })
        .catch(error => {
            console.error('Error restarting game:', error);
            document.getElementById('message-container').innerText = 'Error restarting the game.';
        });
}





function attemptToTakeSeat(seatNumber) {
    const playerNameInput = document.getElementById('playerName');
    const buyInAmount = document.getElementById('buyInAmount').value;
    const playerName = playerNameInput.value.trim();
    if (!playerName) {
        alert('Please enter your name.');
        return;
    }

    // Check if the entered name is already displayed in any of the seats
    const existingPlayerNames = document.querySelectorAll('.player-name');
    for (let i = 0; i < existingPlayerNames.length; i++) {
        if (existingPlayerNames[i].textContent.trim().toLowerCase() === playerName.toLowerCase()) {
            alert('This player name is already taken. Please choose a different name.');
            return;
        }
    }

    takeSeat(playerName, seatNumber, buyInAmount);
}



function takeSeat(playerName, seatNumber, buyInAmount) {
    fetch('/poker/join', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
            // Include CSRF token if needed
        },
        body: JSON.stringify({ playerName, seatNumber, buyInAmount })
    })
        .then(response => {
            if (!response.ok) {
                return response.text().then(text => { throw new Error(text || 'Unable to take seat') });
            }
            return response.json();
        })
        .then(data => {
            const playerNameDisplay = document.getElementById(`player-name-seat-${seatNumber}`);
            playerNameDisplay.textContent = playerName;
            const buyInAmountDisplay = document.getElementById(`buyin-amount-seat-${seatNumber}`);
            buyInAmountDisplay.textContent = `Buy-in: $${buyInAmount}`;

        })
        .catch(error => {
            console.error('Error taking seat:', error);
            alert(error.message);
        });
}

function leaveSeat(seatNumber) {
    fetch('/poker/leave', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
            // Include CSRF token if needed
        },
        body: JSON.stringify({ seatNumber })
    })
        .then(response => {
            if (!response.ok) {
                throw new Error('Failed to leave the seat');
            }
            // Clear the player's name and buy-in amount from the seat
            const playerNameDisplay = document.getElementById(`player-name-seat-${seatNumber}`);
            const buyInAmountDisplay = document.getElementById(`buyin-amount-seat-${seatNumber}`);
            if (playerNameDisplay) {
                playerNameDisplay.textContent = ''; // Clear player name
            }
            if (buyInAmountDisplay) {
                buyInAmountDisplay.textContent = ''; // Clear buy-in amount
            }
        })


        .catch(error => {
            console.error('Error leaving seat:', error);
        });
}

function handleCheck(seatNumber) {
    fetch('/poker/check', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
        },
        body: JSON.stringify({ seatNumber })
    })
        .then(response => response.json())
        .then(data => {
            // Update the UI based on the response
            // This might include enabling action buttons for the next player
            updateGameUI(data.nextPlayerSeatNumber);
            if (data.displayCommunityCards) {
                displayCommunityCards();
            }
        })
        .catch(error => {
            console.error('Error handling check:', error);
            // Handle any errors
        });
}

function updateGameUI(nextPlayerSeatNumber) {
    // Disable all action buttons
    disableAllActionButtons();

    // Enable action buttons only for the next player
    enableActionButtonsForPlayer(nextPlayerSeatNumber);
}

function disableAllActionButtons() {
    const actionButtons = document.querySelectorAll('.player-actions button');
    actionButtons.forEach(button => {
        button.disabled = true;
    });
}

function enableActionButtonsForPlayer(seatNumber) {
    // Find the seat div for the given seatNumber
    const seatDiv = document.getElementById(`seat-${seatNumber}`);

    // Select the bet and check buttons within this div
    const actionButtons = seatDiv.querySelectorAll('.player-actions button');

    // Enable the buttons
    actionButtons.forEach(button => {
        button.disabled = false;
    });
}



function displayCommunityCards() {
    fetch('/poker/communityCards') // Adjust the endpoint as needed
        .then(response => response.json())
        .then(cards => {
            cards.forEach((card, index) => {
                const container = document.getElementById(`card-slot-${index + 1}`);
                const imageFileName = `${card.rank.toLowerCase()}_of_${card.suit.toLowerCase()}.png`;
                const cardImg = document.createElement('img');
                cardImg.src = `images/cards/${imageFileName}`;
                container.innerHTML = ''; // Clear previous card in this slot
                container.appendChild(cardImg);
            });
        })
        .catch(error => console.error('Error fetching community cards:', error));
}
