document.addEventListener('DOMContentLoaded', (event) => {
    document.getElementById('dealCards').addEventListener('click', dealCards);
    document.getElementById('restartGame').addEventListener('click', restartGame);
});
document.getElementById('startGame').addEventListener('click', startGame);
function dealCards() {
    fetch('/poker/deal', {
        method: 'GET',
        headers: {
            'Content-Type': 'application/json',
            // If you have CSRF protection enabled, you need to add the CSRF token here.
        }
    })
        .then(response => response.json())
        .then(data => {
            const gameArea = document.getElementById('gameArea');
            gameArea.innerHTML = JSON.stringify(data, null, 2);
        })
        .catch(error => console.error('Error dealing cards:', error));
}


function restartGame() {
    // Call the backend to restart the game
    fetch('/poker/restart', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
            // If you have CSRF protection enabled, you need to add the CSRF token here.
        }
    })
        .then(response => {
            if (response.ok) {
                // Game restarted successfully, now deal new cards
                dealCards();
            } else {
                throw new Error('Failed to restart the game');
            }
        })
        .catch(error => console.error('Error restarting game:', error));
}


function dealCards() {
    fetch('/poker/deal', {
        method: 'GET',
        headers: {
            'Content-Type': 'application/json',
            // If you have CSRF protection enabled, you need to add the CSRF token here.
        }
    })
        .then(response => response.json())
        .then(data => {
            const gameArea = document.getElementById('gameArea');
            gameArea.innerHTML = ''; // Clear the game area before adding new cards

            data.forEach((playerHand, index) => {
                const playerDiv = document.createElement('div');
                playerDiv.id = `player-${index + 1}`;
                playerDiv.className = 'player-hand';
                playerDiv.innerHTML = `<h3>Player ${index + 1}</h3>`;

                playerHand.forEach(card => {
                    const cardImage = document.createElement('img');
                    cardImage.src = `images/cards/${card.rank}_of_${card.suit}.png`; // Construct the image path
                    playerDiv.appendChild(cardImage);
                });

                gameArea.appendChild(playerDiv);
            });
        })
        .catch(error => console.error('Error dealing cards:', error));
}
function startGame() {
    const numPlayers = document.getElementById('numPlayers').value;
    fetch(`/poker/start?players=${numPlayers}`, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
            // Include CSRF token if needed
        }
    })
        .then(response => {
            if (!response.ok) {
                throw new Error('Failed to start the game');
            }
            return response.json();
        })
        .then(data => {
            const gameArea = document.getElementById('gameArea');
            gameArea.innerHTML = ''; // Clear previous content

            data.forEach((playerHand, index) => {
                const playerDiv = document.createElement('div');
                playerDiv.id = `player-${index + 1}`;
                playerDiv.className = 'player-hand';
                playerDiv.innerHTML = `<h3>Player ${index + 1}</h3>`;

                playerHand.forEach(card => {
                    const cardImage = document.createElement('img');
                    // Update the path to match the location of your card images
                    cardImage.src = `images/cards/${card.rank.toLowerCase()}_of_${card.suit.toLowerCase()}.png`;
                    cardImage.alt = `${card.rank} of ${card.suit}`;
                    playerDiv.appendChild(cardImage);
                });

                gameArea.appendChild(playerDiv);
            });
        })
        .catch(error => {
            console.error('Error starting game:', error);
            document.getElementById('gameArea').innerText = 'Error starting the game.';
        });
}


// Generate seat placeholders
const seatsContainer = document.getElementById('seats');
for (let i = 1; i <= 10; i++) {
    const seatDiv = document.createElement('div');
    seatDiv.id = `seat-${i}`;
    seatDiv.className = 'seat';
    seatDiv.innerHTML = `
        <button onclick="attemptToTakeSeat(${i})">Seat ${i}</button>
        <p id="player-name-seat-${i}"></p> <!-- Placeholder for player name -->
    `;
    seatsContainer.appendChild(seatDiv);
}

// Function to handle attempting to take a seat
function attemptToTakeSeat(seatNumber) {
    const playerNameInput = document.getElementById('playerName');
    const playerName = playerNameInput.value.trim();
    if (!playerName) {
        alert('Please enter your name.');
        return;
    }

    takeSeat(playerName, seatNumber);
}

// Function to take a seat
function takeSeat(playerName, seatNumber) {
    fetch('/poker/join', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
            // Include CSRF token if needed
        },
        body: JSON.stringify({ playerName, seatNumber })
    })
        .then(response => response.json())
        .then(data => {
            if (data.success) {
                // If the player successfully takes a seat, show the name
                document.getElementById(`player-name-seat-${seatNumber}`).innerText = playerName;
                // Disable the button to prevent the seat from being taken again
                document.querySelector(`#seat-${seatNumber} button`).disabled = true;
            } else {
                alert(data.message || 'Unable to take seat');
            }
        })
        .catch(error => {
            console.error('Error taking seat:', error);
        });
}

// Initialize seat dropdown
const seatSelection = document.getElementById('seatSelection');
for (let i = 1; i <= 10; i++) {
    const option = document.createElement('option');
    option.value = i;
    option.text = `Seat ${i}`;
    seatSelection.appendChild(option);
}

// Handle sit down action
document.getElementById('sitDown').addEventListener('click', () => {
    const playerName = document.getElementById('playerName').value.trim();
    const selectedSeat = seatSelection.value;

    if (!playerName) {
        alert('Please enter your name.');
        return;
    }

    if (!selectedSeat) {
        alert('Please select a seat.');
        return;
    }

    sitDown(playerName, selectedSeat);
});

// Function to manage sitting down logic
function sitDown(playerName, seatNumber) {
    // Your existing code to send the playerName and seatNumber to the server

    // On success, update the UI to show the player's name under the selected seat
    const seatDiv = document.getElementById(`seat-${seatNumber}`);
    const playerNameParagraph = seatDiv.querySelector('p') || document.createElement('p');
    playerNameParagraph.textContent = playerName;
    seatDiv.appendChild(playerNameParagraph);

    // Optionally, disable the seat in the dropdown
    const seatOption = document.querySelector(`#seatSelection option[value="${seatNumber}"]`);
    seatOption.disabled = true;
    seatOption.textContent += ' (taken)';
}
