package ru.khromov.library.LibraryBoot.controllers;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import ru.khromov.library.LibraryBoot.models.Book;
import ru.khromov.library.LibraryBoot.models.Person;
import ru.khromov.library.LibraryBoot.services.BookService;
import ru.khromov.library.LibraryBoot.services.PersonService;

@Controller
@RequestMapping("/books")
public class BookController {

    private BookService bookService;
    private PersonService personService;


    @Autowired
    public BookController(PersonService personService, BookService bookService) {
        this.personService = personService;
        this.bookService = bookService;
    }

    @GetMapping
    public String books(Model model, @RequestParam(value = "page",required = false) Integer page,
                        @RequestParam(value = "books_per_page", required = false) Integer booksPerPage,
                        @RequestParam(value = "sort_by_year", required = false) boolean sortByYear) {
        if(page == null || booksPerPage == null) {
            model.addAttribute("books", bookService.findAll(sortByYear));
        }else{
            model.addAttribute("books", bookService.findWithPagination(page, booksPerPage, sortByYear));
        }
        return "books/book";
    }

    @GetMapping("/{id}")
    public String title(@PathVariable("id") int id, Model model, @ModelAttribute("person") Person person) {
        model.addAttribute("book", bookService.findOne(id));

        Person bookOwner = bookService.getBookOwner(id);

        if (bookOwner != null)
            model.addAttribute("owner", bookOwner);
        else
            model.addAttribute("people", personService.findAll());

        return "books/title";
    }

    @GetMapping("/new-book")
    public String newBook(Model model) {
        model.addAttribute("book", new Book());
        return "books/new-book";
    }

    @PostMapping
    public String addBook(@ModelAttribute("book") @Valid Book book, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return "books/new-book";
        }
        bookService.save(book);
        return "redirect:/books";
    }

    @GetMapping("{id}/edit-book")
    public String editBook( Model model, @PathVariable("id") int id) {
        model.addAttribute("book", bookService.findOne(id));
        return "books/edit-book";
    }

    @PatchMapping("/{id}")
    public String updateBook(@ModelAttribute("book") @Valid Book book,
                             BindingResult bindingResult,
                             @PathVariable("id") int id) {
        if (bindingResult.hasErrors()) {
            return "books/edit-book";
        }
        bookService.update(id, book);
        return "redirect:/books";
    }

    @DeleteMapping("/{id}")
    public String deleteBook(@PathVariable("id") int id) {
        bookService.delete(id);
        return "redirect:/books";
    }

    // осовбождает книгу при нажатии на кнопку
    @PatchMapping("/{id}/release")
    public String release(@PathVariable("id") int id){
        bookService.release(id);
        return "redirect:/books/" + id;
    }

    // назначает книгу на кнопку
    @PatchMapping("/{id}/assign")
    public String assign (@PathVariable("id") int id, @ModelAttribute("person") Person selectedPerson){
        // у selectedPerson назначено только поле ID, остальные поля - null
        bookService.assign(id, selectedPerson);
        return "redirect:/books/" + id;
    }

    @GetMapping("/search")
    public String searchPage(){
        return "books/search";
    }

    @PostMapping("/search")
    public String makeSearch(Model model, @RequestParam("query") String query) {
        model.addAttribute("books", bookService.searchByTitle(query));
        return "books/search";
    }
}
