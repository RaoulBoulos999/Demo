package com.sword.oams.service;

import com.sword.oams.domain.*;
import com.sword.oams.payload.request.EmployeeRequest;
import com.sword.oams.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@Transactional
public class EmployeeService {

	@Autowired
	EmployeeRepository employeeRepository;

	@Autowired
	RotationRepository rotationRepository;

	@Autowired
	RoomRepository roomRepository;

	@Autowired
	TeamRepository teamRepository;

	@Autowired
	AuthRolesRepository authRolesRepository;

	@Autowired
	UserRepository userRepository;

	@Autowired
	PasswordEncoder passwordEncoder;

	public Employee addEmployee(EmployeeRequest request) {
		Team team = this.teamRepository.findById(request.getTeamId()).orElse(null);
		Set<AuthenticationRole> roles = new HashSet<>();
		AuthenticationRole userRole = authRolesRepository.findByName(ERole.ROLE_USER).orElse(null);
		roles.add(userRole);

		Employee employee = Employee.builder()
				.firstName(request.getFirstName())
				.lastName(request.getLastName())
				.team(team)
				.status(request.isStatus())
				.build();

		User user = User.builder()
					.resetPasswordToken(null)
					.address("Lebanon")
					.username(request.getFirstName()+request.getLastName())
					.email(request.getFirstName()+"."+request.getLastName()+"@sword-group.com")
					.password(passwordEncoder.encode("Changeme"))
					.roles(roles)
					//.employee(employee)
					.build();



		employee.setUser(user);
		userRepository.save(user);

		return employeeRepository.save(employee);
	}

	public Employee getEmployeeById(Long id) {
		Employee emp = employeeRepository.findById(id).orElseThrow(() -> new RuntimeException("Error: Employee Id Not Found"));
		User user = emp.getUser();
		user.setPassword("Not Available");
		emp.setUser(user);

		return emp;
	}

	public List<Employee> getAllEmployees() {
		List<Employee> emps = employeeRepository.findAll();
		for(Employee emp: emps) {
			User user = emp.getUser();
			user.setPassword("Not Available");
			emp.setUser(user);
		}
		return emps;
	}

	public List<Employee> getAllEmployeesByAvailableStatus() {
		List<Employee> emps = employeeRepository.findByStatus(true);
		for(Employee emp: emps) {
			User user = emp.getUser();
			user.setPassword("Not Available");
			emp.setUser(user);
		}
		return emps;
	}

	public List<Employee> getAllEmployeesByUnavailableStatus() {
		List<Employee> emps = employeeRepository.findByStatus(false);
		for(Employee emp: emps) {
			User user = emp.getUser();
			user.setPassword("Not Available");
			emp.setUser(user);
		}
		return emps;
	}


	public void deleteEmployeeById(Long id) { employeeRepository.deleteById(id); }

	public Employee updateEmployeeById(Long id, EmployeeRequest request) {
		Team team = teamRepository.findById(request.getTeamId()).orElse(null);
		Room room = this.roomRepository.findById(request.getRoomId()).orElse(null);
		RotationGroup rotationGroup = this.rotationRepository.findById(request.getRotationId()).orElse(null);
		return employeeRepository.findById(id)
					.map(employee -> {
						employee.setFirstName(request.getFirstName());
						employee.setLastName(request.getLastName());
						employee.setTeam(team);
						employee.setStatus(request.isStatus());
						employee.setRoom(room);
						employee.setRotationGroup(rotationGroup);

						return employeeRepository.save(employee);
					})
					.orElse(null);
	}
}