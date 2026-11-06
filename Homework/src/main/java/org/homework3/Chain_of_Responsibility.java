package org.homework3;

// Запрос
record BookRequest(String bookTitle, int daysToBorrow) {
}

// Абстрактный обработчик
abstract class ApprovalHandler {
    protected ApprovalHandler nextHandler;

    public void setNext(ApprovalHandler nextHandler) {
        this.nextHandler = nextHandler;
    }

    public abstract void handleRequest(BookRequest request);
}

// Конкретный обработчик 1
class Librarian extends ApprovalHandler {
    @Override
    public void handleRequest(BookRequest request) {
        if (request.daysToBorrow() <= 7) {
            System.out.println("[Цепочка] Библиотекарь одобрил выдачу книги '" + request.bookTitle() + "' на " + request.daysToBorrow() + " дн.");
        } else if (nextHandler != null) {
            nextHandler.handleRequest(request);
        }
    }
}

// Конкретный обработчик 2
class HeadLibrarian extends ApprovalHandler {
    @Override
    public void handleRequest(BookRequest request) {
        if (request.daysToBorrow() <= 30) {
            System.out.println("[Цепочка] Зав. библиотекой одобрил выдачу книги '" + request.bookTitle() + "' на " + request.daysToBorrow() + " дн.");
        } else if (nextHandler != null) {
            nextHandler.handleRequest(request);
        }
    }
}

// Конкретный обработчик 3
class Dean extends ApprovalHandler {
    @Override
    public void handleRequest(BookRequest request) {
        System.out.println("[Цепочка] Декан лично одобрил долгосрочную выдачу книги '" + request.bookTitle() + "' на " + request.daysToBorrow() + " дн.");
    }
}